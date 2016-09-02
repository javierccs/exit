import jenkins.model.*;
import java.util.regex.*;
import util.Utilities;

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))
def utils = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/Utils.groovy"))

String no_spaces(value) {
    return value.trim();
}

String no_spaces_and_lowercase(value) {
    return no_spaces(value).toLowerCase();
}

//Retrieve execution input parameters
def inputData() {
    return [
            gitLabHost             : Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger").getGitlabHostUrl(),
            gitLabApiToken         : Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger").getGitlabApiToken(),
            gitLabProject          : no_spaces("${GITLAB_PROJECT}"),
            gitLabReleaseBranch    : no_spaces("${GIT_RELEASE_BRANCH}"),
            gitLabIntegrationBranch: no_spaces("${GIT_INTEGRATION_BRANCH}"),
            gitLabCredential       : "${GITLAB_CREDENTIAL}",
            openShiftUrl           : no_spaces("${OSE3_URL}"),
            openShiftProjectName   : [
                    dev: no_spaces_and_lowercase("${OSE3_PROJECT_NAME}")
            ],
            ose3TokenDev	   : no_spaces("${OSE3_TOKEN_PROJECT_DEV}"),
            openShiftTemplatePath  : no_spaces("${OSE3_TEMPLATE_PATH}"),
            testCommand            : "${TEST_COMMAND}"
    ];
}

def params = inputData();

final String regex = "((?:(?:ssh|git|https?):\\/\\/)?(?:.+(?:(?::.+)?)@)?[\\w\\.]+(?::\\d+)?\\/)?([^\\/\\s]+)\\/([^\\.\\s]+)(?:\\.git)?"
Pattern pattern = Pattern.compile(regex);
Matcher matcher = pattern.matcher(params.gitLabProject);
assert matcher.matches() : "[ERROR] Syntax error: " + params.gitLabProject + " doesn't match expected url pattern."
def GITLAB_URL = matcher.group(1) ?: params.gitLabHost;
def GROUP_NAME = matcher.group(2);
def REPOSITORY_NAME = matcher.group(3);
out.println("GitLab URL: " + GITLAB_URL);
out.println("GitLab Group: " + GROUP_NAME);
out.println("GitLab Project: " + REPOSITORY_NAME);

def GITLAB_PROJECT = GROUP_NAME + '/' + REPOSITORY_NAME
def GIT_SOURCE_REPO = "origin";
def GIT_INTEGRATION_BRANCH = params.gitLabIntegrationBranch;
def GIT_RELEASE_BRANCH = params.gitLabReleaseBranch;
def buildJobName = GITLAB_PROJECT + '-ci-build';


 //creck gitlab credentials
 def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL)
 if ( gitlabCredsType == null ) {
   throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
 }
 println ("GitLab credential type " + gitlabCredsType );
 

job(buildJobName) {
    label('ose3-deploy')
    logRotator(daysToKeep = 30, numToKeep = 10, artifactDaysToKeep = -1, artifactNumToKeep = -1)
    parameters {
        stringParam('GIT_SOURCE_REPO', GIT_SOURCE_REPO, 'GitLab source repo name (only for MERGE events from forked repositories)');
        stringParam('GIT_INTEGRATION_BRANCH', params.gitLabIntegrationBranch, 'GitLab integration branch');
        stringParam('GIT_RELEASE_BRANCH', params.gitLabReleaseBranch, 'GitLab release branch');
        stringParam('OSE3_URL', params.openShiftUrl, 'Openshift Url');
        stringParam('OSE3_PROJECT_NAME', params.openShiftProjectName.dev, 'Openshift project name');
        stringParam('OSE3_TEMPLATE_PATH', params.openShiftTemplatePath, 'Path to openshift template');
        stringParam('OSE3_TOKEN_PROJECT_DEV', params.ose3TokenDev, 'ose3 token dev');

    }
    scm {
        git {
            branch('${GIT_SOURCE_REPO}/${GIT_INTEGRATION_BRANCH}')
            browser {
                gitLab(params.gitLabHost + GITLAB_PROJECT, '8.6')
            } //browser
            remote {
                credentials(params.gitLabCredential)
                name(GIT_SOURCE_REPO)
                url(GITLAB_URL + GITLAB_PROJECT + '.git')
            } //remote
            extensions {
                wipeOutWorkspace()
            }
        } //git
    } //scm
    triggers {
        gitlabPush {
            buildOnPushEvents(true)
            buildOnMergeRequestEvents(false)
            setBuildDescription(true)
            useCiFeatures(true)
            allowAllBranches(false)
            includeBranches(params.gitLabIntegrationBranch)
        }
    } //triggers

    wrappers {
        credentialsBinding {
		 //If user password credentials are provided bind is required
		 if ( gitlabCredsType == 'UserPassword' ){
		           usernamePassword('GITLAB_CREDENTIAL', GITLAB_CREDENTIAL)
		 }
        }
 //if ssh credentials ssAgent is added
 if ( gitlabCredsType == 'SSH' ){
       sshAgent(GITLAB_CREDENTIAL)
 }
        release {
            postSuccessfulBuildSteps {
                shell("git merge -m \"\${BUILD_DISPLAY_NAME}\" \${GIT_SOURCE_REPO}/\${GIT_INTEGRATION_BRANCH} \${GIT_SOURCE_REPO}/\${GIT_RELEASE_BRANCH}")
            }
            postSuccessfulBuildPublishers {
                git {
                    forcePush(true)
                    branch(GIT_SOURCE_REPO, params.gitLabReleaseBranch)
                    tag(GIT_SOURCE_REPO, "BUILD_\${BUILD_NUMBER}") {
                        create(true)
                    }
                }
                extendedEmail {
                    defaultContent('${DEFAULT_CONTENT}')
                    contentType('text/html')
                    triggers {
                        always()
                        failure {
                            sendTo {
                                culprits()
                            }
                        }
                        unstable {
                            sendTo {
                                culprits()
                            }
                        }
                        fixedUnhealthy {
                            sendTo {
                                developers()
                            }
                        }
                    }
                }
            }
        } //release
    }

    steps {
        shell("install_template_in_ose3.sh");
        shell(params.testCommand);
    }
}

gitlabHooks.GitLabWebHooks(params.gitLabHost, params.gitLabApiToken, GITLAB_PROJECT, buildJobName)
