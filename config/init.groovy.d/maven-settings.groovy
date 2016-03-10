import org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig;
import org.jenkinsci.lib.configprovider.ConfigProvider;
import org.jenkinsci.plugins.configfiles.maven.security.ServerCredentialMapping;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import java.util.logging.Logger
import jenkins.model.*;

def logger = Logger.getLogger("org.apache.maven.artifact.deployer.DefaultArtifactDeployer")
def mavenDeployerLogin = System.getenv("MAVEN_DEPLOYER_LOGIN")
def mavenDeployerPasswd = System.getenv("MAVEN_DEPLOYER_PASSWD")
if (!(mavenDeployerLogin?.trim() && mavenDeployerPasswd?.trim())){
  logger.severe("Maven deployer environment variables (MAVEN_DEPLOYER_LOGIN, MAVEN_DEPLOYER_PASSWD) are not set. Artifact deployment won't work")
} else {
  //Create creds
  def mavenDeployerCredentialsId = "maven-deployer-credentials-id";
  def systemCreds = SystemCredentialsProvider.getInstance();
  Map<Domain, List<Credentials>> domainCredentialsMap = systemCreds.getDomainCredentialsMap();
  
  domainCredentialsMap[Domain.global()].add(
 
    new UsernamePasswordCredentialsImpl(
      CredentialsScope.SYSTEM,
      mavenDeployerCredentialsId,
      'Maven deployer credentials',
      mavenDeployerLogin,
      mavenDeployerPasswd
      )
  )
  systemCreds.save();
  logger.info("Created system credential \"$mavenDeployerCredentialsId\": {username=\"$mavenDeployerLogin\",password=\""+mavenDeployerPasswd.replaceAll('.', '*')+"\"}")
  
  List<ServerCredentialMapping> serverCredentialMappings = new ArrayList<ServerCredentialMapping>();
  serverCredentialMappings.add(new ServerCredentialMapping("serenity", mavenDeployerCredentialsId));
 
  def settingsFile = Jenkins.instance.getRootDir().toString()+'/userContent/customConfigs/maven-settings.xml'
  InputStream is = new FileInputStream(settingsFile)
    
  def mavenSettingsConfig = new MavenSettingsConfig(
    "org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig", 
    "Serenity Maven Settings", "Custom Maven settings for Serenity framework", is.text, true, serverCredentialMappings);
  
  for (ConfigProvider provider : ConfigProvider.all()) {
      if (provider.isResponsibleFor(mavenSettingsConfig.id)) {
          provider.save(mavenSettingsConfig);
          logger.info("Created maven settings file \"Serenity Maven Settings\" from \"$settingsFile\"")
      }
  }

  // Test authentication
  def nexusRepositoryUrl = System.getenv('NEXUS_BASE_URL')
  if (nexusRepositoryUrl==null) {
    nexusRepositoryUrl='http://islinnxp01.scisb.isban.corp:8081/nexus'
  }
  def url = new URL(nexusRepositoryUrl+"/service/local/authentication/login")
  def connection = url.openConnection()
  connection.setRequestMethod("GET")
  connection.setRequestProperty("Authorization", "Basic "+(mavenDeployerLogin+':'+mavenDeployerPasswd).bytes.encodeBase64().toString());
  connection.connect()

  (connection.responseCode == 200)? logger.info('Test Nexus API connection... Success'):logger.severe('Test Nexus API connection... ERROR '+connection.inputStream.withReader { Reader reader -> reader.text })
}
