import jenkins.model.*;

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
            gitLabProject          : no_spaces("${GITLAB_PROJECT}"),
            gitLabReleaseBranch    : no_spaces("${GIT_RELEASE_BRANCH}"),
            gitLabIntegrationBranch: no_spaces("${GIT_INTEGRATION_BRANCH}"),
            openShiftUrl           : no_spaces("${OSE3_URL}"),
            openShiftProjectName   : [
                    dev: no_spaces_and_lowercase("${OSE3_PROJECT_NAME}") + "-dev"
            ],
            openShiftTemplatePath  : no_spaces("${OSE3_TEMPLATE_PATH}"),
            serenityCredential     : "${SERENITY_CREDENTIAL}",
            testCommand            : "${TEST_COMMAND}"
    ];
}

def params = inputData();
def GIT_SOURCE_REPO = "origin";
def GIT_INTEGRATION_BRANCH = params.gitLabIntegrationBranch;
def GIT_RELEASE_BRANCH = params.gitLabReleaseBranch;
def buildJobName = params.gitLabProject + '-ci-build';




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
    }
    scm {
        git {
            branch('${GIT_SOURCE_REPO}/${GIT_INTEGRATION_BRANCH}')
            browser {
                gitLab(params.gitLabHost + '/' + params.gitLabProject, '8.6')
            } //browser
            remote {
                credentials(params.serenityCredential)
                name(GIT_SOURCE_REPO)
                url(params.gitLabHost + '/' + params.gitLabProject + '.git')
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
            usernamePassword('GITLAB_CREDENTIAL', params.serenityCredential)
            usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', params.serenityCredential)
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




