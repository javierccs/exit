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

def dockerCertificatesDirectory = System.getenv('DOCKER_CERTIFICATES_DIRECTORY')

def dockerCertificatesDirectoryCredentialsId = 'docker-certificates-credentials'
def jenkinsSlaveCredentialsId = 'jenkins-ssh-slave-credentials'

///////////////////////////////////////////////////:
// Configure credz
///////////////////////////////////////////////////:
def system_creds = SystemCredentialsProvider.getInstance()

Map<Domain, List<Credentials>> domainCredentialsMap = system_creds.getDomainCredentialsMap()

domainCredentialsMap[Domain.global()].add(

  new UsernamePasswordCredentialsImpl(
    CredentialsScope.SYSTEM,
    jenkinsSlaveCredentialsId,
    'Jenkins slave docker container credentials.',
    'jenkins',
    'jenkins'
    )
)

// domainCredentialsMap[Domain.global()].add(
//
//    new com.nirima.jenkins.plugins.docker.utils.DockerDirectoryCredentials(
//      CredentialsScope.SYSTEM,
//      dockerCertificatesDirectoryCredentialsId,
//      'Contains the certificates required to authenticate against a Docker TLS secured port',
//      dockerCertificatesDirectory
//    )
//)

system_creds.save()
println 'Added docker cloud credentials.'

/////////////////////////////////////////////////////:
// Docker Cloud config per-se
/////////////////////////////////////////////////////:
def CLOUD_NAME = 'cloud'
if (Jenkins.instance.clouds.getByName(CLOUD_NAME) == null) {
  def swarmMasterUrl = System.getenv("SWARM_MASTER_URL")
  assert swarmMasterUrl != null : "SWARM_MASTER_URL env var not set!"

  def docker_settings = [:]
  docker_settings =
  [
    [
      name: CLOUD_NAME,
      serverUrl: swarmMasterUrl,
      containerCapStr: '10',
      connectionTimeout: 5,
      readTimeout: 15,
      credentialsId: '', // dockerCertificatesDirectoryCredentialsId,
      version: '',
      templates: [
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-wordpress-builder:Q1',
          labelString: 'wordpress-build',
          environmentsString: 'JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=jenkins',
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '5',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          memoryLimit: 1024,
          memorySwap: 0,
          cpuShares: 2,
          prefixStartSlaveCmd: '',
          suffixStartSlaveCmd: '',
          instanceCapStr: '1',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: true,
          tty: false,
          macAddress: ''
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-wordpress-docker-image-builder:Q1',
          labelString: 'wordpress-docker',
          environmentsString: 'JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=jenkins',
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '5',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          memoryLimit: 1024,
          memorySwap: 0,
          cpuShares: 2,
          prefixStartSlaveCmd: '',
          suffixStartSlaveCmd: '',
          instanceCapStr: '1',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '/var/run/docker.sock:/var/run/docker.sock\n/usr/local/bin/docker:/usr/local/bin/docker',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: true,
          tty: false,
          macAddress: ''
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-deployer:Q1',
          labelString: 'ose3-deploy',
          environmentsString: 'JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=jenkins',
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '5',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          memoryLimit: 512,
          memorySwap: 0,
          cpuShares: 2,
          prefixStartSlaveCmd: '',
          suffixStartSlaveCmd: '',
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
          macAddress: ''
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-maven:Q1',
          labelString: 'maven',
          environmentsString: 'JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=jenkins',
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '5',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          memoryLimit: 1024,
          memorySwap: 0,
          cpuShares: 2,
          prefixStartSlaveCmd: '',
          suffixStartSlaveCmd: '',
          instanceCapStr: '1',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '/opt/docker-volumes/jslave-maven/m2:/opt/docker-volumes/jslave-maven/m2',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: true,
          tty: false,
          macAddress: ''
        ],
        [
          image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-hpalm-bridge:Q1',
          labelString: 'hpalm_bridge',
          environmentsString: 'JENKINS_USERLOGIN=jenkins\nJENKINS_USERPASSWORD=jenkins',
          remoteFs: '/home/jenkins',
          credentialsId: jenkinsSlaveCredentialsId,
          idleTerminationMinutes: '5',
          sshLaunchTimeoutMinutes: '1',
          jvmOptions: '',
          javaPath: '',
          memoryLimit: 1024,
          memorySwap: 0,
          cpuShares: 2,
          prefixStartSlaveCmd: '',
          suffixStartSlaveCmd: '',
          instanceCapStr: '1',
          dnsString: '',
          dockerCommand: 'start',
          volumesString: '/opt/docker-volumes/jslave-maven/m2:/opt/docker-volumes/jslave-maven/m2',
          volumesFromString: '',
          hostname: '',
          bindPorts: '',
          bindAllPorts: false,
          privileged: true,
          tty: false,
          macAddress: ''
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
          template.memoryLimit,
          template.memorySwap,
          template.cpuShares,
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

      dockerTemplate.setMode(Node.Mode.EXCLUSIVE)
      dockerTemplate.setNumExecutors(2)
      dockerTemplate.setRemoveVolumes(true)
      dockerTemplate.setRetentionStrategy(new DockerCloudRetentionStrategy(5))
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
}
