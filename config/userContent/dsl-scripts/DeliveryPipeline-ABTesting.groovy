/*
  GITLAB_PROJECT: GitLab project name, (like groupname/repositoryname)
*/
import jenkins.model.*

// Input parameters
def (GROUP_NAME, REPOSITORY_NAME) = GITLAB_PROJECT.tokenize('/')

if (!Jenkins.instance.getItemByFullName(GROUP_NAME)) {
  folder(GROUP_NAME) {
  }
}

deliveryPipelineView(GITLAB_PROJECT) {
    allowRebuild()
    columns(1)
    enableManualTriggers()
    pipelineInstances(3)
    showAggregatedPipeline()
    showAvatars()
    showChangeLog()
    showDescription()
    showPromotions()
    showTotalBuildTime()
    updateInterval(5)
    pipelines {
      regex(REPOSITORY_NAME+'-ci-build-(.*)')
    }
} // deliveryPipelineView
