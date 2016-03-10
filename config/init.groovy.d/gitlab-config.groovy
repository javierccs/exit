import jenkins.model.*
import java.util.logging.Logger

def logger = Logger.getLogger("hudson.plugins.git.GitSCM")
def inst = Jenkins.getInstance()
def git = inst.getDescriptor("hudson.plugins.git.GitSCM")
def email = (System.getenv("JENKINS_EMAIL") == null)? "jenkins@serenity.corp":System.getenv("JENKINS_EMAIL")
logger.info("Git global config: {name=\"jenkins\", email=\"$email\"}")
git.setGlobalConfigName("jenkins")
git.setGlobalConfigEmail(email)
git.save()

logger = Logger.getLogger("com.dabsquared.gitlabjenkins.GitLabWebHook")
if (!(System.getenv("GITLAB_API_TOKEN")?.trim() && System.getenv("GITLAB_URL")?.trim())) {
  logger.severe('GitLab environment variables not set. Gitlab access won\'t work')
} else {
  def gitlab = inst.getDescriptorByType(com.dabsquared.gitlabjenkins.GitLabPushTrigger.DescriptorImpl)
  gitlab.gitlabApiToken = System.getenv("GITLAB_API_TOKEN")
  gitlab.gitlabHostUrl = System.getenv("GITLAB_URL")
  gitlab.ignoreCertificateErrors = true
  logger.info("GitLab config: {url=\""+gitlab.getGitlabHostUrl()+"\", token=\""+gitlab.getGitlabApiToken().replaceAll('.', '*')+"\"}")
  gitlab.save()

  //testing connection
  def url = new URL(gitlab.getGitlabHostUrl()+"/api/v3/projects?private_token="+gitlab.getGitlabApiToken())
  def connection = url.openConnection()
  connection.setRequestMethod("GET")
  connection.connect()

  (connection.responseCode == 200)? logger.info('Test GitLab API connection... Success'):logger.severe('Test GitLab API connection... ERROR '+connection.inputStream.withReader { Reader reader -> reader.text })
}
