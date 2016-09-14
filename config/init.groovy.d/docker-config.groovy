import jenkins.model.*
import hudson.model.*

import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.SystemCredentialsProvider

import com.nirima.jenkins.plugins.docker.*
import com.nirima.jenkins.plugins.docker.launcher.*
import com.nirima.jenkins.plugins.docker.strategy.*
import java.util.logging.Logger
import java.util.Random

def logger = Logger.getLogger('com.nirima.jenkins.plugins.docker.DockerCloud')
logger.info("Setting docker cloud...")
def dockerCertificatesDirectory = System.getenv('DOCKER_CERTIFICATES_DIRECTORY')
def dockerCertificatesDirectoryCredentialsId = 'docker-certificates-credentials'
def jenkinsSlaveCredentialsId = 'jenkins-ssh-slave-credentials'

///////////////////////////////////////////////////:
// Configure credz
///////////////////////////////////////////////////:
def system_creds = SystemCredentialsProvider.getInstance()
Map<Domain, List<Credentials>> domainCredentialsMap = system_creds.getDomainCredentialsMap()
def obj = domainCredentialsMap[Domain.global()].find {jenkinsSlaveCredentialsId.equals(it.getId())}
if (obj != null) {
  logger.info("Jenkins slave docker container credentials already exists. Updating...")
  domainCredentialsMap[Domain.global()].remove(obj)
}
Random rand = new Random()
def password = 'jenkins'+rand.nextInt(100000)
domainCredentialsMap[Domain.global()].add(
  new UsernamePasswordCredentialsImpl(
    CredentialsScope.SYSTEM,
    jenkinsSlaveCredentialsId,
    'Jenkins slave docker container credentials.',
    'jenkins',
    password
    )
)
logger.info('Added jenkins slave docker container credentials.')
system_creds.save()
// domainCredentialsMap[Domain.global()].add(
//
//    new com.nirima.jenkins.plugins.docker.utils.DockerDirectoryCredentials(
//      CredentialsScope.SYSTEM,
//      dockerCertificatesDirectoryCredentialsId,
//      'Contains the certificates required to authenticate against a Docker TLS secured port',
//      dockerCertificatesDirectory
//    )
//)

/////////////////////////////////////////////////////:
// Docker Cloud config per-se
/////////////////////////////////////////////////////:
def CLOUD_NAME = 'serenity'
def swarmMasterUrl = System.getenv("SWARM_MASTER_URL")
assert swarmMasterUrl != null : "SWARM_MASTER_URL env var not set!"
def mavenDataContainer = System.getenv("MAVEN_DATA")
def nodeJSDataContainer = System.getenv("NODEJS_DATA")
def defaultRootPathForVolumes = System.getenv("DOCKER_SLAVES_VOLUMES_ROOT")
def inst = Jenkins.instance.clouds.getByName(CLOUD_NAME)
if (inst != null) {
  Jenkins.instance.clouds.remove(inst)
}

def docker_settings = [:]
docker_settings =
  [
    [
      name: CLOUD_NAME,
      serverUrl: swarmMasterUrl,
      containerCapStr: '50',
      connectionTimeout: 5,
      readTimeout: 15,
      credentialsId: '', // dockerCertificatesDirectoryCredentialsId,
      version: '',
      templates: [
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-wordpress-builder:latest',
          labelString: 'wordpress-build',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE 
        ],
	[
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-docker-socket:latest',
          labelString: 'docker',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '/var/run/docker.sock:/var/run/docker.sock\n/usr/bin/docker:/usr/bin/docker\n/usr/lib/x86_64-linux-gnu/libapparmor.so.1.1.0:/usr/lib/x86_64-linux-gnu/libapparmor.so.1\n/lib64/libdevmapper.so.1.02:/usr/lib/libdevmapper.so.1.02',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE 
        ],
	[
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-front-docker-image-builder:latest',
          labelString: 'front-build-docker',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '/var/run/docker.sock:/var/run/docker.sock\n/usr/bin/docker:/usr/bin/docker\n/usr/lib/x86_64-linux-gnu/libapparmor.so.1.1.0:/usr/lib/x86_64-linux-gnu/libapparmor.so.1\n/lib64/libdevmapper.so.1.02:/usr/lib/libdevmapper.so.1.02',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE 
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-wordpress-docker-image-builder:latest',
          labelString: 'wordpress-docker',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '/var/run/docker.sock:/var/run/docker.sock\n/usr/bin/docker:/usr/bin/docker\n/usr/lib/x86_64-linux-gnu/libapparmor.so.1.1.0:/usr/lib/x86_64-linux-gnu/libapparmor.so.1\n/lib64/libdevmapper.so.1.02:/usr/lib/libdevmapper.so.1.02',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-deployer:latest',
          labelString: 'ose3-deploy',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-maven:latest',
          labelString: 'maven',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: (mavenDataContainer?.trim())? '':"$defaultRootPathForVolumes/jslave-maven:/tmp/jslave-maven/m2",
          volumesFromString: (mavenDataContainer?.trim())? mavenDataContainer:'',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-hpalm-bridge:latest',
          labelString: 'hpalm_bridge',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: (mavenDataContainer?.trim())? '':"$defaultRootPathForVolumes/jslave-maven:/tmp/jslave-maven/m2",
          volumesFromString: (mavenDataContainer?.trim())? mavenDataContainer:'',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-nodejs:latest',
          labelString: 'nodejs',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: (nodeJSDataContainer?.trim())? '':"$defaultRootPathForVolumes/nodejs-cache:/cache",
          volumesFromString: (nodeJSDataContainer?.trim())? nodeJSDataContainer:'',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-base:ics',
          labelString: '',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '1',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.NORMAL
        ],
		[
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-apic:latest',
          labelString: 'apic',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '2',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '2',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: false,
          tty: false,
          macAddress: '',
          mode: Node.Mode.EXCLUSIVE
        ]
      ]
    ]
  ]

  def dockerClouds = []
  docker_settings.each { cloud ->

    def templates = []
    cloud.templates.each { template ->
      def dockerTemplateBase =
        new DockerTemplateBase(
          template.image,
          template.dnsString,
          template.dockerCommand,
          template.volumesString,
          template.volumesFromString,
          template.environmentsString,
          template.lxcConfString,
          template.hostname,
          null, //template.memoryLimit,
          null, //template.memorySwap,
          null, //template.cpuShares,
          template.bindPorts,
          template.bindAllPorts,
          template.privileged,
          template.tty,
          template.macAddress
        )

      def dockerTemplate =
        new DockerTemplate(
          dockerTemplateBase,
          template.labelString,
          template.remoteFs,
          template.remoteFsMapping,
          template.instanceCapStr
        )

      def dockerComputerSSHLauncher = new DockerComputerSSHLauncher(
          new hudson.plugins.sshslaves.SSHConnector(22, template.credentialsId, null, null, null, null, null )
      )

      dockerTemplate.setLauncher(dockerComputerSSHLauncher)
      dockerTemplate.setMode(template.mode)
      dockerTemplate.setNumExecutors(1)
      dockerTemplate.setRemoveVolumes(true)
      dockerTemplate.setRetentionStrategy(new DockerCloudRetentionStrategy(2))
      dockerTemplate.setPullStrategy(DockerImagePullStrategy.PULL_LATEST)
      templates.add(dockerTemplate)
    }

    dockerClouds.add(
      new DockerCloud(cloud.name,
                    templates,
                    cloud.serverUrl,
                    cloud.containerCapStr,
                    cloud.connectTimeout ?: 15, // Well, it's one for the money...
                    cloud.readTimeout ?: 15,    // Two for the show
                    cloud.credentialsId,
                    cloud.version
      )
    )
  }

  Jenkins.instance.clouds.addAll(dockerClouds)
  println 'Configured docker cloud.'
