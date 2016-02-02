import jenkins.model.*

// Input parameters
def _PROJECT_NAME_ = "${PROJECT_NAME}".trim()
def _GITLAB_URL_ = "${GITLAB_URL}".trim()
def _MAIL_LIST_ = "${MAIL_LIST}".trim()
def _BRANCH_ = "${GIT_BRANCH}".trim()

def _PROJECT_TYPE_ = "docker"
def inst = Jenkins.getInstance()
def gitlab = inst.getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
def _GITLAB_SERVER_ = gitlab.getGitlabHostUrl()
def _GITLAB_API_TOKEN_ = gitlab.getGitlabApiToken()
def _GITLAB_PROJECT_ = _GITLAB_URL_.minus(_GITLAB_SERVER_+'/')
def JOB_NAME = "wp-${_PROJECT_NAME_.replace(" ","_")}"

def _REPOSITORY_='${ENV,var=\"REPOSITORY\"}'

def credentialsId
def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
  com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class,
  inst,
  null,
  null
);
for (c in creds) {
  if (c.description.equals('GreenLight Jenkins access')) {
     credentialsId = c.id
    }
  }

// Build job
job (JOB_NAME+'-build') {
	println "JOB: ${JOB_NAME}"
    label(_PROJECT_TYPE_)
    deliveryPipelineConfiguration('CI', 'Build Image')

    logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)
                
		// Gives permission for the special authenticated group to see the workspace of the job
/*	authorization {
		permission('hudson.model.Item.Build', "${BUILD_USER_ID}")
		permission('hudson.model.Item.Cancel', "${BUILD_USER_ID}")
        permission('hudson.model.Item.Delete', "${BUILD_USER_ID}")
		permission('hudson.model.Item.Discover', "${BUILD_USER_ID}")
		permission('hudson.model.Item.Read', "${BUILD_USER_ID}")
		permission('hudson.model.Item.Workspace', "${BUILD_USER_ID}")
		permission('hudson.model.Run.Update', "${BUILD_USER_ID}")
		//permission('hudson.plugins.release.ReleaseWrapper.Release', "${BUILD_USER_ID}")	  
	} //authorization */

    parameters {
		// Defines a simple text parameter, where users can enter a string value.
		stringParam('gitlabActionType', 'PUSH', null)
  		stringParam('gitlabSourceRepoURL', _GITLAB_URL_, null)
  		stringParam('gitlabSourceRepoName', 'origin', null)
  		stringParam('gitlabSourceBranch', _BRANCH_, null)
  		stringParam('gitlabTargetBranch', _BRANCH_, null)
    }
  
	scm {
		git {
				// Specify the branches to examine for changes and to build.
			branch('${gitlabSourceRepoName}/${gitlabSourceBranch}')
				// Adds a repository browser for browsing the details of changes in an external system.
			browser {
				gitLab(_GITLAB_URL_, '7.9')
			} //browser
				// Adds a remote.
			remote {
					// Sets credentials for authentication with the remote repository.
				credentials(credentialsId)
					// Sets a name for the remote.
				name('origin')
					// Sets the remote URL.
				url(_GITLAB_URL_)
			} //remote
          
            wipeOutWorkspace(true)
            mergeOptions('origin', '${gitlabTargetBranch}')
	} //git
	} //scm

	triggers {
        gitlabPush {
            buildOnPushEvents(true)
            setBuildDescription(true)
            useCiFeatures(false)
            allowAllBranches(false)
            includeBranches(_BRANCH_)
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
'export REGISTRY_BASE_URL=https://registry.lvtc.gsnet.corp\n'+
'#Need Parameters\n'+
'# JOB_NAME\n'+
'# JOB_INSTANCE\n'+
'export TAG_GIT=0.\\$BUILD_TAG-SNAPSHOT;\n'+
'# IMAGE MUST BE THE NAME OF GITLAB PROJECT\n'+
'REMOTE_REPO=\"\$(git remote -v|tail -n1)\"\n'+
'# GITLAB project\n'+
'IMAGE_NAME=\$(echo \$REMOTE_REPO| sed \"s|.git .*||\" |sed \"s|.*/||\")\n'+
'# Group GITLAB\n'+
'GROUP_GITLAB=\$(echo \$REMOTE_REPO|sed \"s|.git .*||\" | sed \"s|\$IMAGE_NAME||\" | sed \"s|/\$||\" | sed \"s|.*/||\")\n'+
'IMAGE_NAME_BASE=\"\$(grep -HR \"image:\" \$FILE_DOCKER_COMPOSE | cut -f3 -d\':\'| head -n1)\";\n'+
'IMAGE_NAME_BASE=\"\${IMAGE_NAME_BASE#\"\${IMAGE_NAME_BASE%%[![:space:]]*}\"}\";   # elimina los espacios por delante\n'+
'IMAGE_NAME_BASE=\"\${IMAGE_NAME_BASE%\"\${IMAGE_NAME_BASE##*[![:space:]]}\"}\";  # elimina los espacios por detrán\n'+
'if [ \"\$IMAGE_NAME\" == \"\" ]; then\n'+
'        echo \"[ERROR] Name Docker Image doesnt exist\"\n'+
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
'                \\n RUN mkdir -p /usr/src/wordpress/uploads \\\n'+
'                \\n RUN if ls \$WPRESS_DATA_HOME/uploads; then cp -rd \$WPRESS_DATA_HOME/uploads/* /usr/src/wordpress/wp-content/uploads; fi \\\n'+
'                \\n RUN chown -R www-data:www-data /usr/src/wordpress\"\n'+
'fi\n'+
'echo \"DOCKER_FILE:\"\$WORDPRESS_DOCKERFILE\n'+
'echo -e \$WORDPRESS_DOCKERFILE > Dockerfile\n'+
'export REPOSITORY=\$GROUP_GITLAB/\$IMAGE_NAME\n'+
'export DOCKER_HOST=\"unix:///var/run/docker.sock\"\n'+
'echo \"REPOSITORY=\"\$REPOSITORY > env.properties\n'+
'echo \"TAG_GIT=\"\$TAG_GIT >> env.properties\n'+
'echo \"DOCKER_HOST=\"\$DOCKER_HOST >> env.properties\n'+
'echo \"REPOSITORY:\"\$REPOSITORY\n'+
'echo \"TAG_GIT:\"\$TAG_GIT\n'+
'echo \"DOCKER_HOST:\"\$DOCKER_HOST\n'+
'echo \"\$(docker ps)\"\n'+
'echo \"\$(id)\" '
			)
			
		environmentVariables {
            propertiesFile('env.properties')
        }	
		
	 
      dockerBuildAndPublish {
			dockerRegistryURL("https://registry.lvtc.gsnet.corp")
            repositoryName(_REPOSITORY_)
            tag('0.$BUILD_TAG-SNAPSHOT')
            registryCredentials(credentialsId)
            forcePull(false)
            createFingerprints(false)
            skipDecorate()
        }
      
    }// steps
	 publishers {
         git {
            pushOnlyIfSuccess()
            tag('origin', '0.$BUILD_TAG-SNAPSHOT') {
                message('DOCKER IMAGE TAG')
                create()
            }
        }
        downstream(JOB_NAME+'-dev-deploy', 'SUCCESS')
    } //publishers

} //job

job (JOB_NAME+'-dev-deploy') {
    deliveryPipelineConfiguration('Dev', 'Deploy image')
    steps {
        shell('echo Hello!')
    }
}

deliveryPipelineView(PROJECT_NAME) {
    allowPipelineStart()
	allowRebuild()
    columns(3)
    enableManualTriggers()
    pipelineInstances(3)
    showAggregatedPipeline()
    showAvatars()
    showChangeLog()
	showDescription()
	showPromotions()
	showTotalBuildTime()
    updateInterval(10)   
    pipelines {
        component(PROJECT_NAME, JOB_NAME+'-build')
    }
} // deliveryPipelineView

def url = new URL(_GITLAB_SERVER_+"/api/v3/projects/"+java.net.URLEncoder.encode(_GITLAB_PROJECT_)+"/hooks?"+
                  "private_token="+_GITLAB_API_TOKEN_+
                  "&url="+inst.getRootUrl()+"project/"+JOB_NAME+
                  "&merge_requests_events=true&push_events=true")
println "Create hook: "+url

/*def connection = url.openConnection()
connection.setRequestMethod("POST")
connection.doOutput = true
connection.connect()
	
println connection.content.text
assert connection.responseCode == 201*/
