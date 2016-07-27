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
            openShiftProjectName   : no_spaces_and_lowercase("${OSE3_PROJECT_NAME}"),
            serenityCredential     : "${SERENITY_CREDENTIAL}"
    ];
}

def params = inputData();
println "Params: $params";
def buildJobName = params.gitLabProject + '-ci-build';

job(buildJobName) {

    println "JOB: " + buildJobName
    label('ose3-deploy')
    logRotator(daysToKeep = 30, numToKeep = 10, artifactDaysToKeep = -1, artifactNumToKeep = -1)
    parameters {
        stringParam('gitlabSourceRepoName', 'origin', 'GitLab source repo name (only for MERGE events from forked repositories)')
    }
    scm {
        git {
            branch('${gitlabSourceRepoName}/${gitlabSourceBranch}')
            browser {
                gitLab(params.gitLabHost + '/' + params.gitLabProject, '8.6')
            } //browser
            remote {
                credentials(params.serenityCredential)
                name('origin')
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

    publishers {


        extendedEmail {
            defaultContent('${JELLY_SCRIPT, template="static-analysis.jelly"}')
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
        } //extendedEmail
    } //publishers

    wrappers {
        credentialsBinding {
            usernamePassword('GITLAB_USERNAME', 'GITLAB_PASSWORD', params.serenityCredential)
        }
        credentialsBinding {
            usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', params.serenityCredential)
        }
        buildName(params.gitLabProject.tokenize('/')[1] + ':${ENV,var="${BUILD_NUMBER}')
        release {

            postSuccessfulBuildSteps {
                shell("git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}")
            }
            preBuildSteps {
                environmentVariables {
                    env('IS_RELEASE',true)
                }
            }
        } //release
    }
}




