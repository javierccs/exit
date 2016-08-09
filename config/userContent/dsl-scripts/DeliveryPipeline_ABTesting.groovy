/*
  GITLAB_PROJECT: GitLab project name, (like groupname/repositoryname)
*/
import jenkins.model.*
import java.util.regex.*

// Input parameters
final String REGEXP = "(?:(?:(?:ssh|git|https?):\\/\\/)?(?:.+(?:(?::.+)?)@)?[\\w\\.]+(?::\\d+)?\\/)?([^\\/\\s]+)\\/([^\\.\\s]+)(?:\\.git)?"
Pattern pattern = Pattern.compile(REGEXP);
Matcher matcher = pattern.matcher(GITLAB_PROJECT);
assert matcher.matches() : "[ERROR] Syntax error: " + GITLAB_PROJECT + " doesn't match expected url pattern."
def GROUP_NAME = matcher.group(1)
def REPOSITORY_NAME = matcher.group(2)
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
