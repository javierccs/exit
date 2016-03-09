import org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig;
import org.jenkinsci.lib.configprovider.ConfigProvider;
import org.jenkinsci.plugins.configfiles.maven.security.ServerCredentialMapping;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import jenkins.model.*;
  
println "-->Setting up maven"

def mavenDeployerLogin = System.getenv("MAVEN_DEPLOYER_LOGIN")
def mavenDeployerPasswd = System.getenv("MAVEN_DEPLOYER_PASSWD")

if (mavenDeployerLogin == null && mavenDeployerPasswd == null){
  println "WARNING: Not configuring Maven as 'mavenDeployerLogin' and 'mavenDeployerPasswd' environment variables are not set"
}else{
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
  
  List<ServerCredentialMapping> serverCredentialMappings = new ArrayList<ServerCredentialMapping>();
  serverCredentialMappings.add(new ServerCredentialMapping("serenity", mavenDeployerCredentialsId));
 
  InputStream is = new FileInputStream(Jenkins.instance.getRootDir().toString()+'/userContent/customConfigs/maven-settings.xml')
    
  def globalMavenSettingsConfig = new GlobalMavenSettingsConfig(
    "org.jenkinsci.plugins.configfiles.maven.job.MvnSettingsProvider", 
    "Serenity Maven Settings", "Custom Maven settings for Serenity framework", is.text, true, serverCredentialMappings);
  
  for (ConfigProvider provider : ConfigProvider.all()) {
      if (provider.isResponsibleFor(globalMavenSettingsConfig.id)) {
          provider.save(globalMavenSettingsConfig);
      }
  }
}
