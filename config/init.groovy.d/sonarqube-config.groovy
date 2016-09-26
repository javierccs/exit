import jenkins.model.Jenkins
import java.util.logging.Logger
import hudson.plugins.sonar.*

String NAME="Serenity SonarQube"
def logger = Logger.getLogger("hudson.plugins.sonar")
logger.info("Setting SonarQube installation: $NAME")
String serverUrl=System.getenv("SONARQUBE_SERVER_URL")
String sonarLogin=System.getenv("SONARQUBE_SERVER_LOGIN")
String sonarPassword=System.getenv("SONARQUBE_SERVER_PASSWORD")
String databaseUrl=System.getenv("SONARQUBE_DATABASE_URL")
String databaseLogin=System.getenv("SONARQUBE_DATABASE_LOGIN")
String databasePassword=System.getenv("SONARQUBE_DATABASE_PASSWORD")

if(!(serverUrl?.trim() && sonarLogin?.trim() && sonarPassword?.trim() &&
   databaseUrl?.trim() && databaseLogin?.trim() && databasePassword?.trim())) {
  logger.warning("SonarQube environment variables aren't set. SonarQube setting cancelled.")
  return
}

def sonarqube = Jenkins.getInstance().getDescriptor("hudson.plugins.sonar.SonarGlobalConfiguration")
def sinst = new SonarInstallation(NAME, serverUrl, "4.6", "", databaseUrl, databaseLogin, databasePassword,
  '', '', null,   sonarLogin, sonarPassword, '')

int i = 0
def oldsinst = sonarqube.getInstallations().find {NAME.equals(it.getName())}
if (oldsinst != null) {
  logger.info("SonarQube installation already exists: $NAME. Updating...")
  sonarqube.getInstallations() - oldsinst
}

sonarqube.setInstallations(sinst)
sonarqube.setBuildWrapperEnabled(true)
sonarqube.save()
logger.info("SonarQube installation added: $NAME")