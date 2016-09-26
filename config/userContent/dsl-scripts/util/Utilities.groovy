package util
import jenkins.model.*
import java.util.regex.*

class Utilities {
  static final CREDENTIAL_SSH = "SSH";
  static final CREDENTIAL_USERPASSWORD = "UserPassword";
  static final GIT_URL_REGEX_PATTERN =  "((?:(?:ssh|git|https?):\\/\\/)?(?:.+(?:(?::.+)?)@)?[\\w\\.-]+(?::\\d+)?\\/)?([^\\/\\s]+)\\/([^\\.\\s]+)(?:\\.git)?"
  /**
   * Return 'SSH' if given credential id is a SSH credential or CREDENTIAL_USERPASSWORD
   * if it is a User password credential otherwise it throws IllegalArgumentException
   * if credential does not exist returns null
   */
  static String getCredentialType(def credentialId) {
    def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
      com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class,
      Jenkins.instance,
      null,
      null
    );
    def gitlabCredsType = null;
    for (c in creds) {
      if ( c.id == credentialId ){
        if (c.class.getName() ==  'com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey' ){
          gitlabCredsType = CREDENTIAL_SSH
        }else if ( c.class.getName() ==  'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl'){
          gitlabCredsType = CREDENTIAL_USERPASSWORD
        }else{
          throw new javaposse.jobdsl.dsl.DslException ("ERROR: GitLab '" + credentialId  + "' credentials  type '" + c.class.getName() + "' not supported! ")
        }
      }
    }
    return gitlabCredsType;
  }
  //Return GitLab group and project 
  //from URL. If given URL is not valid
  //Assertion fails
  //returns Map with
  //"url", "groupName"  and "repositoyName"
  static Map parseGitlabUrl (def gitUrl){
    // Input parameters
    Pattern pattern = Pattern.compile(GIT_URL_REGEX_PATTERN);
    Matcher matcher = pattern.matcher(gitUrl);
    assert matcher.matches() : "[ERROR] Syntax error: " + gitUrl + " doesn't match expected url pattern.";
    def gitServerUrl = matcher.group(1) ?: Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger").getGitlabHostUrl();
    return [url: gitServerUrl,  groupName:  matcher.group(2), repositoryName:  matcher.group(3)]
  }

}
