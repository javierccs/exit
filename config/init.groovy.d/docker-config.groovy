import java.util.logging.Logger
import jenkins.model.*
import hudson.model.*

import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.SystemCredentialsProvider

import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.jenkins.plugins.docker.DockerTemplate
import com.nirima.jenkins.plugins.docker.DockerTemplateBase
import com.nirima.jenkins.plugins.docker.DockerImagePullStrategy
import com.nirima.jenkins.plugins.docker.launcher.DockerComputerLauncher
import com.nirima.jenkins.plugins.docker.launcher.DockerComputerSSHLauncher
import com.nirima.jenkins.plugins.docker.strategy.DockerCloudRetentionStrategy

/**
 *  
 * @author n90460
 *
 */
public interface Configurable1 {

	void doConfig()
}

/**
 * 
 * @author n90460
 */
abstract class AbstractConfig1 implements Configurable1 {

	private Jenkins jenkins = null

	/**
	 * Gets the Jenkins singleton
	 * @return the Jenkins instance
	 * @see Jenkins#getInstance()
	 */
	public Jenkins getInstance() {
		if (jenkins == null) jenkins = Jenkins.getInstance()
		return jenkins;
	}

	/**
	 * Sets the Jenkins instance. Use this method for Unit Testing purposes
	 * @param jenkins	the Jenkins instance
	 */
	public void setInstance(Jenkins jenkins) {
		this.jenkins = jenkins;
	}

	/**
	 * Replaces the first object in the array that matches the closure condition by the object passed as argument. 
	 * If there isn't any match then the object is added to the end of the array. 
	 * @param array		the array to be modified
	 * @param obj		the object to add
	 * @param closure	the filter to perform a match on the array
	 * @return the modified array
	 */
	public <T> T[] replace (T[] array, T obj, Closure closure) {
		int i = array.findIndexOf (closure)
		if (i < 0) array = array + obj
		else array[i] = obj

		return array
	}
	
	/**
	 * Replaces the first object in the array that matches the closure condition by the object passed as argument. 
	 * If there isn't any match then the object is added to the end of the array.
	 * @param list	the list to be modified 
	 * @param obj	the object to add
	 * @param closure	the filter to perform a match on the list
	 * @return	the modified list
	 */
	public <E> List<E> replace (List<E> list, E obj, Closure closure) {
		int i = list.findLastIndexOf (closure)
		if (i < 0) list << obj
		else list.set(i, obj)

		return list
	}

	/**
	 * Adds a slash to the end of the path if it doesn't already end with a slash.
	 * @param path	The path to add the trailing slash
	 * @return		A path with trailing slash at the end
	 */
	public String addTrailingSlash(String path) {
		if (path == null) return null
		if (path.endsWith('/')) return path
		else return path + '/'
	}

	/**
	 * Method that includes all the code to configure the Jenkins instance
	 */
	public abstract void doConfig()
}

/**
 * Adds the docker plugin configuration into a Jenkins instance
 * @author n90460
 */
public class DockerCloudConfig extends AbstractConfig1 {
	private static final Logger logger = Logger.getLogger('com.nirima.jenkins.plugins.docker.DockerManagement')

	private static final String DOCKER_REGISTRY_CREDENTIALID = 'docker-registry-credential-id'
	private static final String SLAVE_CREDENTIALID = 'jenkins-ssh-slave-credentials'

	static final String DEFAULT_CREDENTIALID = ''
	static final String DEFAULT_VERSION = ''
	static final int DEFAULT_CONTAINER_CAP = Integer.MAX_VALUE
	static final int DEFAULT_CONNECT_TIMEOUT = 10
	static final int DEFAULT_READ_TIMEOUT = 60
	static final int IDLE_TERMINATION_MINUTES = 5

	static final DockerCloudRetentionStrategy dockerCloudRetentionStrategy = new DockerCloudRetentionStrategy(IDLE_TERMINATION_MINUTES)

	private DockerComputerLauncher dockerComputerLauncher = null
	private String tenantName
	private List cloudConfig
	private String slaves_username = "jenkins"
	private String slaves_password
	private String docker_username
	private String docker_password

	/**
	 * Creates a new DockerCloudConfig object from a reader of docker clouds
	 * @param tenantName	Jenkins instance name
	 * @param username		Docker Registry username
	 * @param password		Docker Registry password
	 * @param reader		reader
	 */
	public DockerCloudConfig(String tenantName, String username, String password, Reader reader) {
		this(tenantName, username, password, new ConfigSlurper().parse(reader.text).cloud)
	}

	/**
	 * Creates a new DockerCloudConfig object from a list of docker clouds
	 * @param tenantName
	 * @param username
	 * @param password
	 * @param cloudConfig
	 */
	public DockerCloudConfig(String tenantName, String username, String password, List cloudConfig) {
		this.tenantName = tenantName
		this.cloudConfig = cloudConfig
		this.docker_username = username
		this.docker_password = password
	}

	/**
	 * Gets the default DockerComputerLauncher
	 * @return the default DockerComputerLauncher instance
	 */
	public DockerComputerLauncher getDockerLauncher() {
		if (dockerComputerLauncher == null) {
			dockerComputerLauncher = new DockerComputerSSHLauncher(
					new hudson.plugins.sshslaves.SSHConnector(22, SLAVE_CREDENTIALID, null, null, null, null, null )
					)
		}
		return dockerComputerLauncher;
	}

	/**
	 * Sets the default DockerComputerLancher to be used in DockerTemplate configurations
	 * @param dockerComputerLauncher
	 */
	public void setDockerLauncher(DockerComputerLauncher dockerComputerLauncher) {
		this.dockerComputerLauncher = dockerComputerLauncher;
	}

	DockerCloud newDockerCloud(Map cloud) {
		List<DockerTemplate> dockerTemplates = new ArrayList<>()
		for (template in cloud.templates) {
			dockerTemplates.add(newDockerTemplate(template))
		}

		return new DockerCloud(cloud.name,  dockerTemplates, cloud.serverUrl,
				cloud.containerCap ?: DEFAULT_CONTAINER_CAP,
				cloud.connectTimeout ?: DEFAULT_CONNECT_TIMEOUT,
				cloud.readTimeout ?: DEFAULT_READ_TIMEOUT,
				cloud.credentialsId ?: DEFAULT_CREDENTIALID,
				cloud.version ?: DEFAULT_VERSION)
	}

	DockerTemplate newDockerTemplate(Map template) {
		DockerTemplateBase dockerTemplateBase = new DockerTemplateBase(
				template.image,
				template.dnsString ?: '',
				template.network ?: null,
				template.dockerCommand ?: 'start' ,
				template.volumesString ?: '',
				template.volumesFromString ?: '',
				(template.environmentsString ?: '')+
				"\nJENKINS_USERLOGIN=$slaves_username\nJENKINS_USERPASSWORD=$slaves_password"+
				"\nTENANT_NAME=$tenantName\nSERVICE_IGNORE=true",
				template.lxcConfString ?: null,
				template.hostname ?: '',
				template.memoryLimit ?: null,
				template.memorySwap ?: null,
				template.cpuShares ?: null,
				template.bindPorts ?: '',
				template.bindAllPorts ?: false,
				template.privileged ?: false,
				template.tty ?: false,
				template.macAddress ?: '')

		DockerTemplate dockerTemplate = new DockerTemplate(
				dockerTemplateBase,
				template.labelString,
				template.remoteFs ?: '/home/jenkins',
				template.remoteFsMapping ?: '/var/jenkins_home',
				template.instanceCapStr ?: '')

		dockerTemplate.setLauncher(getDockerLauncher())
		dockerTemplate.setRetentionStrategy(
				template.idleMinutes ? new DockerCloudRetentionStrategy(template.idleMinutes) : dockerCloudRetentionStrategy)
		dockerTemplate.setMode(template.mode ?: Node.Mode.EXCLUSIVE)
		dockerTemplate.setNumExecutors(template.numExecutors ?: 2)
		dockerTemplate.setRemoveVolumes(template.removeVolumes ?: true)
		dockerTemplate.setPullStrategy(/*template.pullStrategy ?: */DockerImagePullStrategy.PULL_LATEST)

		return dockerTemplate
	}

	@Override
	public void doConfig() {
		Random rand = new Random()
		slaves_password = 'jenkins'+rand.nextInt(100000)
		def systemCreds = SystemCredentialsProvider.getInstance()
		replace(systemCreds.getDomainCredentialsMap()[Domain.global()],
				new UsernamePasswordCredentialsImpl(
				CredentialsScope.SYSTEM,
				SLAVE_CREDENTIALID,
				'Jenkins slave docker container credentials.',
				'jenkins',
				slaves_password)) {
					SLAVE_CREDENTIALID.equals(it.getId())
				}
		logger.info('Added jenkins slave docker container credentials.')
		replace(systemCreds.getDomainCredentialsMap()[Domain.global()],
				new UsernamePasswordCredentialsImpl(
				CredentialsScope.GLOBAL,
				DOCKER_REGISTRY_CREDENTIALID,
				'Docker registry credential',
				docker_username,
				docker_password)) {
					DOCKER_REGISTRY_CREDENTIALID.equals(it.getId())
				}

		logger.info('Added docker registry credential.')

		for(cloud in cloudConfig) {
			def dockerCloud = newDockerCloud(cloud)
			getInstance().clouds.removeIf {
				it.name == dockerCloud.name
			}
			getInstance().clouds.add(dockerCloud)
		}
		logger.info('Configured docker cloud.')
		systemCreds.save()
		getInstance().save()
	}
}

// Get the Docker Plugin configuration from environment variables
// Reads the Docker clouds configuration from a file in JENKINS_HOME
def tenantName = System.getenv("SERENITY_TENANT") ?: "unnamed"
def jenkinsHome = System.getenv("JENKINS_HOME") ?: "/var/jenkins_home"
def dockerRegistryUsername = System.getenv('DOCKER_REGISTRY_USERNAME').trim()
def dockerRegistryPassword = System.getenv('DOCKER_REGISTRY_PASSWORD').trim()
DockerCloudConfig ddc = new DockerCloudConfig(tenantName, dockerRegistryUsername, dockerRegistryPassword,
		new FileReader(jenkinsHome + "/com.nirima.jenkins.plugins.docker.DockerPluginConfiguration.groovy"))
		ddc.doConfig()

