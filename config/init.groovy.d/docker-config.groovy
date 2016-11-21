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

def logger = Logger.getLogger('com.nirima.jenkins.plugins.docker.DockerCloud')
logger.info("Setting docker cloud...")
def jenkinsSlaveCredentialsId = 'jenkins-ssh-slave-credentials'
def dockerRegistryCredentialId = 'docker-registry-credential-id'
//def dockerRegistryUrl = System.getenv('DOCKER_REGISTRY_BASE_URL') ?: 'https://registry.lvtc.gsnet.corp'
def dockerRegistryUsername = System.getenv('DOCKER_REGISTRY_USERNAME').trim()
def dockerRegistryPassword = System.getenv('DOCKER_REGISTRY_PASSWORD').trim()
def nexusRepositoryUrl = System.getenv('NEXUS_BASE_URL') ?: 'https://nexus.ci.gsnet.corp/nexus'
def mavenGroupRepository = System.getenv('NEXUS_MAVEN_GROUP') ?: '/content/groups/public/'
def npmGroupRepository = System.getenv('NPM_REGISTRY')
def bowerGroupRepository = System.getenv('BOWER_REGISTRY')
def nexus_user = System.getenv('MAVEN_DEPLOYER_LOGIN')
def nexus_password = System.getenv('MAVEN_DEPLOYER_PASSWD')

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
obj = domainCredentialsMap[Domain.global()].find {dockerRegistryCredentialId.equals(it.getId())}
if (obj != null) {
  logger.info("Docker registry credential already exists. Updating...")
  domainCredentialsMap[Domain.global()].remove(obj)
}
domainCredentialsMap[Domain.global()].add(
  new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    dockerRegistryCredentialId,
    'Docker registry credential',
    dockerRegistryUsername,
    dockerRegistryPassword
    )
)
logger.info('Added docker registry credential.')
system_creds.save()

/////////////////////////////////////////////////////:
// Docker Cloud config per-se
/////////////////////////////////////////////////////:
def CLOUD_NAME = 'serenity'
def swarmMasterUrl = System.getenv("SWARM_MASTER_URL")
assert swarmMasterUrl != null : "SWARM_MASTER_URL env var not set!"
def mavenDataContainer = System.getenv("MAVEN_DATA")
def nodeJSDataContainer = System.getenv("NODEJS_DATA")
def defaultRootPathForVolumes = System.getenv("DOCKER_SLAVES_VOLUMES_ROOT")
def tenantName = System.getenv("SERENITY_TENANT")
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
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: "$defaultRootPathForVolumes/sonar:/tmp/.sonar",
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
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password\nNEXUS_BASE_URL=${nexusRepositoryUrl}\nNEXUS_MAVEN_GROUP=${mavenGroupRepository}",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '',
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
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: (mavenDataContainer?.trim())? '':"$defaultRootPathForVolumes/jslave-maven:/tmp/jslave-maven/m2\n$defaultRootPathForVolumes/sonar:/tmp/.sonar",
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
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '',
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
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password"+
              ((npmGroupRepository == null)? '':"\nNPM_REGISTRY=$npmGroupRepository")+
              ((bowerGroupRepository == null)? '':"\nBOWER_REGISTRY=$bowerGroupRepository"),
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: (nodeJSDataContainer?.trim())? '':"$defaultRootPathForVolumes/nodejs-cache:/cache\n$defaultRootPathForVolumes/sonar:/tmp/.sonar",
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
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-builder:latest',
          labelString: '',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: "$defaultRootPathForVolumes/sonar:/tmp/.sonar",
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
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-cordova:latest',
          labelString: 'cordova',
          environmentsString: "JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=$password\nMAVEN_DEPLOYER_LOGIN=$nexus_user\nMAVEN_DEPLOYER_PASSWD=$nexus_password",
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '',
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
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          instanceCapStr: '',
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
          null, //template.network,
          template.dockerCommand,
          template.volumesString,
          template.volumesFromString,
          template.environmentsString+"\nTENANT_NAME=$tenantName",
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


      final int idleTerminationMinutes = 180
      dockerTemplate.setLauncher(dockerComputerSSHLauncher)
      dockerTemplate.setMode(template.mode)
      dockerTemplate.setNumExecutors(2)
      dockerTemplate.setRemoveVolumes(true)
      dockerTemplate.setRetentionStrategy(new DockerCloudRetentionStrategy(idleTerminationMinutes))
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
  logger.info('Configured docker cloud.')
