/*
  GITLAB_PROJECT: GitLab project name, (like groupname/repositoryname)
*/

// Input parameters
def (GROUP_NAME, REPOSITORY_NAME) = GITLAB_PROJECT.tokenize('/')

folder(GROUP_NAME) {
    primaryView(REPOSITORY_NAME)
}

deliveryPipelineView(GITLAB_PROJECT) {
    //allowPipelineStart()
    allowRebuild()
    columns(1)
    //enableManualTriggers()
    pipelineInstances(3)
    showAggregatedPipeline()
    showAvatars()
    showChangeLog()
    showDescription()
    showPromotions()
    showTotalBuildTime()
    updateInterval(5)
    pipelines {
        regex(REPOSITORY_NAME+'-(.*)-build')
    }
} // deliveryPipelineView
