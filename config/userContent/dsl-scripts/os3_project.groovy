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


job(buildJobName) {
    def params = inputData();
    println "JOB: " + buildJobName
    println "Params: $params";
    label('os3')
    logRotator(daysToKeep = 30, numToKeep = 10, artifactDaysToKeep = -1, artifactNumToKeep = -1)
    parameters {
        stringParam('gitlabActionType', 'PUSH', 'GitLab Event (PUSH or MERGE)')
        stringParam('gitlabSourceRepoURL', params.gitLabHost + '/' + params.gitLabProject + '.git', 'GitLab Source Repository')
        stringParam('gitlabSourceRepoName', 'origin', 'GitLab source repo name (only for MERGE events from forked repositories)')
        stringParam('gitlabSourceBranch', params.gitLabIntegrationBranch, 'Gitlab source branch (only for MERGE events from forked repositories)')
        stringParam('gitlabTargetBranch', params.gitLabIntegrationBranch, 'GitLab target branch (only for MERGE events)')
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
            includeBranches(param.gitLabIntegrationBranch)
        }
    } //triggers
}




