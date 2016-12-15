import jenkins.model.*
import org.jenkinsci.plugins.ParameterizedRemoteTrigger.RemoteJenkinsServer
import org.jenkinsci.plugins.ParameterizedRemoteTrigger.RemoteBuildConfiguration
import net.sf.json.JSONObject
import java.util.logging.Logger

def logger = Logger.getLogger "hudson.plugins.git.GitSCM"

/*
 * configuramos el tenant OCC para la ejecución remota de jobs parametrizados.
 * la configuración inicial no lleva credenciales y la URL ha de ser pasada
 * como variable de entorno de nombre REMOTE_TENANT_URL
 */
String remoteTenantUrl = System.getenv('REMOTE_TENANT_URL')
if(remoteTenantUrl!=null){
	JSONObject authenticationMode = new JSONObject();
	authenticationMode.put("value", "none");
	JSONObject auth = new JSONObject();
	auth.put("authenticationMode", authenticationMode);
			  
	logger.info("Configuring parameterized remote trigger plugin whit OCC Tenant")
	RemoteJenkinsServer remoteJenkinsServer = new RemoteJenkinsServer(remoteTenantUrl, "Jenkins OCC-OLC", false, auth)
	RemoteBuildConfiguration.DescriptorImpl descriptor =
		Jenkins.instance.getDescriptorByType(RemoteBuildConfiguration.DescriptorImpl.class)

	descriptor.setRemoteSites(remoteJenkinsServer)
	descriptor.save()
	logger.info("Configured")
}else{
	logger.warning("No optional env var REMOTE_TENANT_URL defined, movin on")
}
