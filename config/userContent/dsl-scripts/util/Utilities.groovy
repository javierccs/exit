package util
import jenkins.model.*

class Utilities {
  static final CREDENTIAL_SSH = "SSH";
  static final CREDENTIAL_USERPASSWORD = "UserPassword";
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
}
