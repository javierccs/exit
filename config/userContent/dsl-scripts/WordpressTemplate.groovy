import jenkins.model.*
  
// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_BRANCH = "${GIT_BRANCH}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim()
def SERENITY_CREDENTIAL = "${SERENITY_CREDENTIAL}"

// Static values
def inst = Jenkins.getInstance()
def gitlab = inst.getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
def GITLAB_SERVER = gitlab.getGitlabHostUrl()
def GITLAB_API_TOKEN = gitlab.getGitlabApiToken()
def JOB_NAME = 'wp-'+GITLAB_PROJECT.replace('/','.')
def PROJECT_NAME = GITLAB_PROJECT.substring(GITLAB_PROJECT.indexOf('/')+1)

// Build job
job (JOB_NAME+'-build') {
	println "JOB: ${JOB_NAME}-build"
    label('docker')
    deliveryPipelineConfiguration('CI', 'Build Image')

    logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)
                
    parameters {
		// Defines a simple text parameter, where users can enter a string value.
		stringParam('gitlabActionType', 'PUSH', null)
  		stringParam('gitlabSourceRepoURL', GITLAB_SERVER+'/'+GITLAB_PROJECT+'.git', null)
  		stringParam('gitlabSourceRepoName', 'origin', null)
  		stringParam('gitlabSourceBranch', GIT_BRANCH, null)
  		stringParam('gitlabTargetBranch', GIT_BRANCH, null)
    }
    wrappers {
        deliveryPipelineVersion(GITLAB_PROJECT+':0.${BUILD_NUMBER}-SNAPSHOT', true)
    }
  
	scm {
		git {
				// Specify the branches to examine for changes and to build.
			branch('${gitlabSourceRepoName}/${gitlabSourceBranch}')
				// Adds a repository browser for browsing the details of changes in an external system.
			browser {
				gitLab(GITLAB_SERVER+'/'+GITLAB_PROJECT, '8.2')
			} //browser
				// Adds a remote.
			remote {
					// Sets credentials for authentication with the remote repository.
				credentials(SERENITY_CREDENTIAL)
					// Sets a name for the remote.
				name('origin')
					// Sets the remote URL.
				url(GITLAB_SERVER+'/'+GITLAB_PROJECT+'.git')
			} //remote
	    } //git
	} //scm

	triggers {
        gitlabPush {
            buildOnPushEvents(true)
            setBuildDescription(true)
            useCiFeatures(false)
            allowAllBranches(false)
            includeBranches(GIT_BRANCH)
        }
	} //triggers
  
    steps {
      shell(
'#!/bin/bash\n'+
'#echo off\n'+
'export HOST_CHECKING=\" -o StrictHostKeyChecking=no\"\n'+
'export FILE_DOCKER_COMPOSE=\"docker-compose.yml\"\n'+
'export DOCKER_FILE=\"DockerFile\"\n'+
'export HOST_REGISTRY_BASE_IP=registry.lvtc.gsnet.corp\n'+
'export REGISTRY_BASE_LOGIN=registry.lvtc.gsnet.corp/\n'+
'#Need Parameters\n'+
'# JOB_NAME\n'+
'# JOB_INSTANCE\n'+
'# IMAGE MUST BE THE NAME OF GITLAB PROJECT\n'+
'IMAGE_NAME_BASE=\"\$(grep -HR \"image:\" \$FILE_DOCKER_COMPOSE | cut -f3 -d\':\'| head -n1)\";\n'+
'IMAGE_NAME_BASE=\"\${IMAGE_NAME_BASE#\"\${IMAGE_NAME_BASE%%[![:space:]]*}\"}\";   # elimina los espacios por delante\n'+
'IMAGE_NAME_BASE=\"\${IMAGE_NAME_BASE%\"\${IMAGE_NAME_BASE##*[![:space:]]}\"}\";  # elimina los espacios por detrán'+
'if [ \"\$IMAGE_NAME_BASE\" == \"\" ]; then\n'+
'        echo \"[ERROR] Name Docker Image doesn´t exist\"\n'+
'        exit 1;\n'+
'fi;\n'+
'echo \"BASE IMAGE NAME:\"\$IMAGE_NAME_BASE\n'+
'IMAGE_VERSION=\"\$(grep -HR \"image:\" \$FILE_DOCKER_COMPOSE | cut -f4 -d\':\'| head -n1)\";\n'+
'IMAGE_VERSION=\"\${IMAGE_VERSION#\"\${IMAGE_VERSION%%[![:space:]]*}\"}\";   # elimina los espacios por delante\n'+
'IMAGE_VERSION=\"\${IMAGE_VERSION%\"\${IMAGE_VERSION##*[![:space:]]}\"}\";  # elimina los espacios por detrán'+
'if [ \"\$IMAGE_VERSION\" == \"\" ]; then\n'+
'        IMAGE_VERSION=\"latest\";\n'+
'fi;\n'+
'echo \"version_imagen:\"\$IMAGE_VERSION\n'+
'WPRESS_DATA_HOME=/tmp/data\n'+
'# moviendo ficheros del workspace al temporal\n'+
'if [ -a \"\$DOCKER_FILE\" ] # Review if void\n'+
'        then\n'+
'                echo \"Docker file alerady exist\";\n'+
'                export WORDPRESS_DOCKERFILE=\"\$(cat \$DOCKER_FILE)\"\n'+
'        else\n'+
'                export WORDPRESS_DOCKERFILE=\"FROM \$IMAGE_NAME_BASE:\$IMAGE_VERSION \\\n'+
'                \\n MAINTAINER serenity-alm@serenity.com \\\n'+
'                \\n ENV WPRESS_DATA_HOME  \$WPRESS_DATA_HOME \\\n'+
'                \\n\\nRUN mkdir \$WPRESS_DATA_HOME \\\n'+
'                \\n ADD ./wp-content \$WPRESS_DATA_HOME \\\n'+
'                \\n RUN if ls \$WPRESS_DATA_HOME/plugins; then cp -rd \$WPRESS_DATA_HOME/plugins/* /usr/src/wordpress/wp-content/plugins/; fi \\\n'+
'                \\n RUN if ls \$WPRESS_DATA_HOME/themes; then cp -rd \$WPRESS_DATA_HOME/themes/* /usr/src/wordpress/wp-content/themes/; fi \\\n'+
'                \\n RUN mkdir -p /usr/src/wordpress/wp-content/uploads \\\n'+
'                \\n RUN if ls \$WPRESS_DATA_HOME/uploads; then cp -rd \$WPRESS_DATA_HOME/uploads/* /usr/src/wordpress/wp-content/uploads; fi \\\n'+
'                \\n RUN chown -R www-data:www-data /usr/src/wordpress \\\n'+
'                \\n LABEL com.serenity.imageowner=\\\"Serenity-ALM\\\" \\\n'+
'                \\n LABEL com.serenity.description=\\\"Docker file generated by Serenity ALM - Wordpress base\\\" \\\n'+
'                \\n LABEL com.serenity.image.version=\\\"0.\$BUILD_TAG-SNAPSHOT\\\" \\\n'+
'                \\n ENV com.serenity.imageowner=\\\"Serenity-ALM\\\" \\\n'+
'                \\n ENV com.serenity.description=\\\"Docker file generated by Serenity ALM - Wordpress base\\\" \\\n'+
'                \\n ENV com.serenity.image.version=\\\"0.\$BUILD_TAG-SNAPSHOT\\\" \"\n'+
'fi\n'+
'echo \"DOCKER_FILE:\"\$WORDPRESS_DOCKERFILE\n'+
'echo -e \$WORDPRESS_DOCKERFILE > Dockerfile\n'+
'export REPOSITORY='+GITLAB_PROJECT+'\n'+
'export DOCKER_HOST=\"unix:///var/run/docker.sock\"\n'+
'echo \"REPOSITORY=\"\$REPOSITORY > env.properties\n'+
'echo \"DOCKER_HOST=\"\$DOCKER_HOST >> env.properties\n'+
'echo \"REPOSITORY:\"\$REPOSITORY\n'+
'echo \"DOCKER_HOST:\"\$DOCKER_HOST\n'+
'echo \"\$(docker ps)\"\n'+
'echo \"\$(id)\" '
			)
			
		environmentVariables {
            propertiesFile('env.properties')
        }	
		
	 
      dockerBuildAndPublish {
			dockerRegistryURL("https://registry.lvtc.gsnet.corp")
            repositoryName(GITLAB_PROJECT)
            tag('0.$BUILD_NUMBER-SNAPSHOT')
            registryCredentials(SERENITY_CREDENTIAL)
            forcePull(false)
            createFingerprints(false)
            skipDecorate()
        }
      
    }// steps
	 publishers {
         git {
            pushOnlyIfSuccess()
            tag('origin', '0.$BUILD_NUMBER-SNAPSHOT') {
                message('DOCKER IMAGE TAG')
                create()
            }
        }
        downstreamParameterized {
            trigger(JOB_NAME+'-dev-deploy') {
                condition('SUCCESS')
                parameters {
                  predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME)
                  predefinedProp('OSE3_CREDENTIAL', SERENITY_CREDENTIAL)
                }
            }
        }
    } //publishers

} //job

job (JOB_NAME+'-dev-deploy') {
    println "JOB: "+JOB_NAME+'-dev-deploy'
    deliveryPipelineConfiguration('Dev', 'Deploy image')
    label('ose3-deploy')
    parameters {
      stringParam('OSE3_PROJECT_NAME', '', '')
      credentialsParam('OSE3_CREDENTIAL') {
            type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
            required()
            description('Username/password for deploying images into Openshift3')
            defaultValue(SERENITY_CREDENTIAL)
        }
      stringParam('PIPELINE_VERSION','','')
    }
    wrappers {
      credentialsBinding {
        usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', '${OSE3_CREDENTIAL}')
      }
    }
    properties {
        sidebarLinks {
            // use uploaded image
            link('http://'+PROJECT_NAME+'-'+OSE3_PROJECT_NAME+'.appls.boae.paas.gsnetcloud.corp', 'Openshift', '/userContent/openshift_64x64.png')
        }
    }
    steps {
        shell('deploy_in_ose3.sh ${OSE3_USERNAME} ${OSE3_PASSWORD} ${OSE3_PROJECT_NAME} '+
              PROJECT_NAME+' wordpress-btsync-external-mysql --param=APP_NAME=\''+PROJECT_NAME+
              '\',WORDPRESS_IMAGE=registry.lvtc.gsnet.corp/${PIPELINE_VERSION}'+
              ',MYSQL_DB_HOST=external-mysql,MYSQL_DB_PORT=3306,MYSQL_DB_USER=admin,MYSQL_DB_PASSWORD=aquielpassword,MYSQL_DB_NAME=wordpress')
    }
}

deliveryPipelineView(JOB_NAME) {
    allowPipelineStart()
	allowRebuild()
    columns(3)
    //enableManualTriggers()
    pipelineInstances(3)
    showAggregatedPipeline()
    showAvatars()
    showChangeLog()
	showDescription()
	showPromotions()
	showTotalBuildTime()
    updateInterval(10)   
    pipelines {
        component(GITLAB_PROJECT, JOB_NAME+'-build')
    }
} // deliveryPipelineView

def url = new URL(GITLAB_SERVER+"/api/v3/projects/"+java.net.URLEncoder.encode(GITLAB_PROJECT)+"/hooks?"+
                  "private_token="+GITLAB_API_TOKEN)

def connection = url.openConnection()
connection.setRequestMethod("GET")
connection.doOutput = true
connection.connect()
	
def text = connection.content.text
assert connection.responseCode == 200

webhook = Jenkins.getInstance().getRootUrl()+"project/"+JOB_NAME+"-build"
if (!text.contains("url\":\""+webhook+"\"")) {
    url = new URL(GITLAB_SERVER+"/api/v3/projects/"+java.net.URLEncoder.encode(GITLAB_PROJECT)+"/hooks?"+
      "private_token="+GITLAB_API_TOKEN+"&url="+webhook+
      "&merge_requests_events=true&push_events=true")
  connection = url.openConnection()
  connection.setRequestMethod("POST")
  connection.doOutput = true
  connection.connect()
  assert connection.responseCode == 201
  println "New hook: "+webhook
}
