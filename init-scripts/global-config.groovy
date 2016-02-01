import hudson.model.*;
import jenkins.model.*;

def inst = Jenkins.getInstance()
def email = (System.getenv("JENKINS_EMAIL") == null)? "jenkins@serenity.corp":System.getenv("JENKINS_EMAIL")
def passwd = (System.getenv("JENKINS_PASSWORD") == null)? "admin" : System.getenv("JENKINS_PASSWORD")
def public_key = (System.getenv("JENKINS_PUBLIC_KEY") == null)? "" : System.getenv("JENKINS_PUBLIC_KEY")

// set_security
println "--> setting jenkins security"
def realm = Jenkins.getInstance().getSecurityRealm()
if (realm instanceof hudson.security.HudsonPrivateSecurityRealm) {
  def adminUser = realm.createAccount("admin",passwd)
  adminUser.setFullName("Administrator")
  adminUser.addProperty(new hudson.tasks.Mailer.UserProperty(email))
  if (public_key != "" ) {
    adminUser.addProperty(new org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl(public_key))
  }
  adminUser.save()
}
