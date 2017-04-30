import static hudson.model.Node.Mode.*

def CLOUD_NAME = 'serenity'
def swarmMasterUrl = System.getenv("SWARM_MASTER_URL") ?: "unix:///var/run/docker.sock"
def defaultRootPathForVolumes = System.getenv('DOCKER_SLAVES_VOLUMES_ROOT') ?: "/opt/docker-volumes"
def dockerRegistryUrl = System.getenv('DOCKER_REGISTRY_BASE_URL') ?: "https://registry.lvtc.gsnet.corp"
def nexusRepositoryUrl = System.getenv('NEXUS_BASE_URL') ?: "https://nexus.alm.gsnetcloud.corp"
def mavenGroupRepository = System.getenv('NEXUS_MAVEN_GROUP') ?: "/repository/maven-public/"
def npmGroupRepository = System.getenv('NPM_REGISTRY') ?: "$nexusRepositoryUrl/repository/npm-group/"
def bowerGroupRepository = System.getenv('BOWER_REGISTRY') ?: "$nexusRepositoryUrl/repository/bower-group/"
def webRepository = System.getenv('WEB_REPOSITORY') ?: "$nexusRepositoryUrl/repository/web/"
def webRepositoryDev = System.getenv('WEB_REPOSITORY_SNAPSHOTS') ?: "$nexusRepositoryUrl/repository/web-snapshots/"
def mavenDataContainer = System.getenv("MAVEN_DATA") ?: ''
def nodeJSDataContainer = System.getenv("NODEJS_DATA") ?: ''

/*
 * Docker Cloud settings example
 cloud = [
   [
     name: 'name', // Provide a name for this Docker Cloud (required)
     serverUrl: 'tcp://localhost:2345', // The URL to use to access your Docker server API (required)
     containerCap: 2147483643, // max containers that is allowed to run (optional)
     connectionTimeout: 10, // Timeout for opening connection to Docker API (optional)
     readTimeout: 60, // Read timeout to Docker API (optional)
     credentialsId: '', // Docker server credential ID (optional),
     version: '', // API version (optional)
     templates: [
      [
	   image: 'SOURCE_IMAGE:TAG', // hash or tagged name of the image to run (required)
	   labelString: 'IMAGE_LABEL', // Tags to group slaves, separated by a space (optional)
	   remoteFs: '/home/jenkins', // Root directory for the Jenkins user to use (optional)
	   remoteFsMapping: '/var/jenkins_home', // location containing the workspace folder on Jenkins master (optional)
	   instanceCapStr: '' // Max. instances of the image to run, empty for unlimited (optional)
	   idleMinutes: 5, // Number of minutes of idleness after which to kill the slave (optional)
	   mode: EXCLUSIVE, // How Jenkins schedules builds on this node : <EXCLUSIVE|NORMAL> (optional)
       numExecutors: 2, // number of executors on every slave (optional)
       removeVolumes: true, // Remove the volumes associated to the container during container remove (optional)
	   dnsString: '', // DNS servers to use, if not set will use Docker host DNS settings (optional)
       network: null, // (optional)
	   dockerCommand: 'start' , // command to run for this image, defaults to "start" (optional)
	   volumesString: '', // New line separated list of volume mounts : <host/path>[<container/path>[:<ro|rw>]] (optional)
	   volumesFromString: '', // New line separated list of containers to inherit volume mounts : <container name>[:<ro|rw>] (optional)
	   lxcConfString: null, // (optional)
	   hostname: '', // (optional)
	   memoryLimit: null, // constrain the memory available to a container in MB (optional)
	   memorySwap: null, // constrain the swap memory available to a container (optional)
	   cpuShares: null, // increase the priority of this container (optional)
	   bindPorts: '', // Bind ports from inside the container to outside of the host : <hostport>:<port> (optional)
	   bindAllPorts: false, // (optional)
	   privileged: false, // Remove the volumes associated to the container (optional)
	   tty: false, // (optional)
	   macAddress: '' // Container MAC address (optional)
	  ],
	  []
     ]
   ],
   []
 ]

 */

cloud = [
  [
    name: CLOUD_NAME,
    serverUrl: swarmMasterUrl,
    connectionTimeout: 10,
    readTimeout: 60,
    templates: [
      [
        image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-docker-socket:1.4.1',
        labelString: 'docker-serenity',
        volumesString: '/var/run/docker.sock:/var/run/docker.sock\n/usr/bin/docker:/usr/bin/docker\n/usr/lib/x86_64-linux-gnu/libapparmor.so.1.1.0:/usr/lib/x86_64-linux-gnu/libapparmor.so.1\n/lib64/libdevmapper.so.1.02:/usr/lib/libdevmapper.so.1.02',
      ],
      [
        image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-deployer:1.4.0',
        labelString: 'ose3-deploy-serenity',
        environmentsString: "NEXUS_BASE_URL=${nexusRepositoryUrl}\nNEXUS_MAVEN_GROUP=${mavenGroupRepository}",
      ],
      [
        image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-maven:1.4.1',
        labelString: 'maven-serenity',
		idleMinutes: 15,
        environmentsString: "NEXUS_BASE_URL=${nexusRepositoryUrl}\nNEXUS_MAVEN_GROUP=${mavenGroupRepository}",
        volumesString: (mavenDataContainer?.trim())? '':"$defaultRootPathForVolumes/jslave-maven:/tmp/jslave-maven/m2\n$defaultRootPathForVolumes/sonar:/tmp/.sonar",
		volumesFromString: (mavenDataContainer?.trim())? mavenDataContainer:''
      ],
      [
        image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-nodejs:1.4.1',
        labelString: 'nodejs-serenity',
        environmentsString: "WEB_REGISTRY=$webRepository\nWEB_REGISTRY_DEV=$webRepositoryDev" +
              ((npmGroupRepository == null)? '':"\nNPM_REGISTRY=$npmGroupRepository")+
              ((bowerGroupRepository == null)? '':"\nBOWER_REGISTRY=$bowerGroupRepository"),
        volumesString: (nodeJSDataContainer?.trim())? '':"$defaultRootPathForVolumes/nodejs-cache:/cache\n$defaultRootPathForVolumes/sonar:/tmp/.sonar",
        volumesFromString: (nodeJSDataContainer?.trim())? nodeJSDataContainer:''
      ],
      [
        image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-builder:1.4.0',
        labelString: '',
        volumesString: "$defaultRootPathForVolumes/sonar:/tmp/.sonar",
        mode: NORMAL
      ],
      [
        image: 'registry.lvtc.gsnet.corp/serenity-alm/jslave-apic:latest',
        labelString: 'apic-serenity'
      ]
    ]
  ]
]
