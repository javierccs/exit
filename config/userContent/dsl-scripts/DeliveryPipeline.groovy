/*
  GITLAB_PROJECT: GitLab project name, (like groupname/repositoryname)
  JENKINS_PROJECT: Jenkins project name
*/

// Input parameters
def JENKINS_PROJECT = "${JENKINS_PROJECT}".trim()

deliveryPipelineView(JENKINS_PROJECT) {
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
        regex(JENKINS_PROJECT+'-(.*)-build')
    }
} // deliveryPipelineView
