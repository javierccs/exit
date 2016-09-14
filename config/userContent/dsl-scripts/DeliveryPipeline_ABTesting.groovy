/*
  GITLAB_PROJECT: GitLab project name, (like groupname/repositoryname)
*/
import jenkins.model.*
import util.Utilities;

// Input parameters
//checks gitlab url
def gitLabMap = Utilities.parseGitlabUrl(GITLAB_PROJECT);
def GROUP_NAME = gitLabMap.groupName
def REPOSITORY_NAME = gitLabMap.repositoryName
out.println("GitLab Group: " + GROUP_NAME);
out.println("GitLab Project: " + REPOSITORY_NAME);
def GITLAB_PROJECT = GROUP_NAME + '/' + REPOSITORY_NAME

if (!Jenkins.instance.getItemByFullName(GROUP_NAME)) {
  folder(GROUP_NAME) {
  }
}

deliveryPipelineView(GITLAB_PROJECT) {
    allowRebuild()
    columns(1)
    enableManualTriggers()
    pipelineInstances(1)
    showAggregatedPipeline()
    showAvatars()
    showChangeLog()
    showDescription()
    showPromotions()
    showTotalBuildTime()
    updateInterval(5)
    pipelines {
      component('feature-A', REPOSITORY_NAME+'-ci-build-feature-A')
      component('feature-B', REPOSITORY_NAME+'-ci-build-feature-B')
    }
} // deliveryPipelineView
