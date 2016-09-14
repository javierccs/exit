import jenkins.model.*;
import hudson.util.Secret;
import java.util.logging.Logger;
import com.dabsquared.gitlabjenkins.connection.*;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.*;

def logger = Logger.getLogger("hudson.plugins.git.GitSCM")
def inst = Jenkins.getInstance()
def git = inst.getDescriptor("hudson.plugins.git.GitSCM")
def email = (System.getenv("JENKINS_EMAIL") == null)? "jenkins@serenity.corp":System.getenv("JENKINS_EMAIL")
logger.info("Git global config: {name=\"jenkins\", email=\"$email\"}")
git.setGlobalConfigName("jenkins")
git.setGlobalConfigEmail(email)
git.save()

logger = Logger.getLogger("com.dabsquared.gitlabjenkins.connection.GitLabConnectionConfig")
if (!(System.getenv("GITLAB_API_TOKEN")?.trim() && System.getenv("GITLAB_URL")?.trim())) {
  logger.severe('GitLab environment variables not set. Gitlab access won\'t work')
} else {
  def gitLabConfig = Jenkins.getInstance().getDescriptorByType(com.dabsquared.gitlabjenkins.connection.GitLabConnectionConfig);
  def GITLAB_NAME = "Serenity GitLab"
  def GITLAB_URL = (System.getenv("GITLAB_URL").endsWith('/'))? System.getenv("GITLAB_URL") : System.getenv("GITLAB_URL") + '/';
  def GITLAB_API_TOKEN = System.getenv("GITLAB_API_TOKEN") 
  logger.info("GitLab config: {name=$GITLAB_NAME, url=\"$GITLAB_URL\", token=\""+GITLAB_API_TOKEN.replaceAll('.', '*')+"\"}")

  // Creating global credential
  def serenityGitlabCredentialId = "serenity-gitlab-credential-id";
  def systemCreds = SystemCredentialsProvider.getInstance();
  Map<Domain, List<Credentials>> domainCredentialsMap = systemCreds.getDomainCredentialsMap();
  def obj = domainCredentialsMap[Domain.global()].find {serenityGitlabCredentialId.equals(it.getId())}
  if (obj != null) {
    logger.info("Serenity GitLab credential already exists. Updating...")
    domainCredentialsMap[Domain.global()].remove(obj)
  }

  domainCredentialsMap[Domain.global()].add(
    new GitLabApiTokenImpl(
      CredentialsScope.GLOBAL,
      "serenity-gitlab-credential-id",
      'Serenity GitLab credential',
      Secret.fromString(GITLAB_API_TOKEN)
      )
  )
  systemCreds.save();

  //testing connection
  def result = gitLabConfig.doTestConnection(GITLAB_URL, serenityGitlabCredentialId, true, 10, 10)
  if (result.toString().startsWith("OK")) {
    logger.info("Test $GITLAB_NAME API connection... " + result)
    def gitlab = new GitLabConnection(GITLAB_NAME, GITLAB_URL, serenityGitlabCredentialId, true, 10, 10)
    gitLabConfig.addConnection(gitlab)
    gitLabConfig.save()
  } else {
    logger.severe("Test $GITLAB_NAME API connection... " + result)
    System.exit(-1)
  }
}
