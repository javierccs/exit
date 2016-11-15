import jenkins.model.Jenkins
import java.util.logging.Logger
import hudson.plugins.sonar.*

String NAME="Serenity SonarQube"
def logger = Logger.getLogger("hudson.plugins.sonar")
String serverUrl=System.getenv("SONARQUBE_SERVER_URL")
String sonarLogin=System.getenv("SONARQUBE_SERVER_LOGIN")
String sonarPassword=System.getenv("SONARQUBE_SERVER_PASSWORD")
String databaseUrl=System.getenv("SONARQUBE_DATABASE_URL")
String databaseLogin=System.getenv("SONARQUBE_DATABASE_LOGIN")
String databasePassword=System.getenv("SONARQUBE_DATABASE_PASSWORD")

if(!(serverUrl?.trim() && sonarLogin?.trim() && sonarPassword?.trim() &&
   databaseUrl?.trim())) {
  logger.warning("SonarQube environment variables aren't set. SonarQube setting cancelled.")
  return
}

// SonarQube Server configuration
logger.info("Setting SonarQube installation: $NAME")
def sonarqube = Jenkins.getInstance().getDescriptor("hudson.plugins.sonar.SonarGlobalConfiguration")
def sinst = new SonarInstallation(NAME, serverUrl, hudson.plugins.sonar.utils.SQServerVersions.SQ_5_1_OR_LOWER, "", databaseUrl, databaseLogin, databasePassword,
  '', '', null,   sonarLogin, sonarPassword, '')

def oldsinst = sonarqube.getInstallations().find {NAME.equals(it.getName())}
if (oldsinst != null) {
  logger.info("SonarQube installation already exists: $NAME. Updating...")
  sonarqube.getInstallations() - oldsinst
}

sonarqube.setInstallations(sinst)
sonarqube.setBuildWrapperEnabled(true)
sonarqube.save()
logger.info("SonarQube installation added: $NAME")

// SonarQube Runner configuration
logger.info("Setting SonarQube Runner installation: $NAME Runner")
def sonarqubeRunner = Jenkins.getInstance().getDescriptor("hudson.plugins.sonar.SonarRunnerInstallation")
def srinst = new SonarRunnerInstallation("$NAME Runner", "/usr/share/sonar-runner", null)
sonarqubeRunner.setInstallations(srinst)
sonarqubeRunner.save()
logger.info("SonarQube Runner installation added: $NAME Runner")
