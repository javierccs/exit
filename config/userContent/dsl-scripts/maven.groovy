import jenkins.model.*

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_INTEGRATION_BRANCH = "${GIT_INTEGRATION_BRANCH}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim()
def SERENITY_CREDENTIAL = "${SERENITY_CREDENTIAL}"

// Static values
def gitlab = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
def GITLAB_SERVER = gitlab.getGitlabHostUrl()
def (GROUP_NAME, REPOSITORY_NAME) = GITLAB_PROJECT.tokenize('/')
def buildJobName = GITLAB_PROJECT+'-ci-build'
def deployDevJobName = GITLAB_PROJECT+'-ose3-dev-deploy'
def deployPreJobName = GITLAB_PROJECT+'-ose3-pre-deploy'
def deployProJobName = GITLAB_PROJECT+'-ose3-pro-deploy'
def nexusRepositoryUrl = System.getenv('NEXUS_BASE_URL')
if (nexusRepositoryUrl==null) {
  nexusRepositoryUrl='https://nexus.ci.gsnet.corp/nexus'
}

//HPALM INFO
def ADD_HPALM_AT_DEV = "${ADD_HPALM_AT_DEV}".trim()
def ADD_HPALM_AT_PRE = "${ADD_HPALM_AT_PRE}".trim()
def _HPALM_TEST_SET_ID_ = "${HPALM_TEST_SET_ID}".trim()
def _HPALM_DOMAIN_ = "${HPALM_DOMAIN}".trim()
def _HPALM_PROJECT_ = "${HPALM_PROJECT}".trim()
def _HPALM_URL_ = "${HPALM_URL}".trim()
def _HPALM_CREDS_ = "${HPALM_CREDS}".trim()
def _TEST_RESULT_PATH_ = "target/surefire-reports"
def _POM_PATH_ = "pom.xml"
def GITLAB_PROJECT_TEST = "${GITLAB_PROJECT_TEST}".trim()
def URL_BASE_SELENIUM= "${URL_BASE_SELENIUM}".trim()

def BridgeHPALMJobName = GITLAB_PROJECT+'-pre-hpalm-bridge'
def BridgeHPALMJobNameDEV = GITLAB_PROJECT+'-dev-hpalm-bridge'

// APP_name for OSE3 -it doesnt allow uppercase chars!!
def APP_NAME_OSE3="${APP_NAME_OSE3}".trim().toLowerCase()
if(APP_NAME_OSE3 == "")
 APP_NAME_OSE3=REPOSITORY_NAME.toLowerCase()

//JAVASE TEMPLATE VARS
def OTHER_OSE3_TEMPLATE_PARAMS =""
JAVA_OPTS_EXT="${JAVA_OPTS_EXT}".trim()
JAVA_PARAMETERS="${JAVA_PARAMETERS}".trim()
POD_MAX_MEM="${POD_MAX_MEM}".trim()
TZ="${TZ}".trim()
WILY_MOM_FQDN="${WILY_MOM_FQDN}".trim()
WILY_MOM_PORT="${WILY_MOM_PORT}".trim()

//Compose the template params, if blank we left the default pf PAAS
if(JAVA_OPTS_EXT != "")
 OTHER_OSE3_TEMPLATE_PARAMS+=",JAVA_OPTS_EXT="+JAVA_OPTS_EXT
if(JAVA_PARAMETERS != "")
 OTHER_OSE3_TEMPLATE_PARAMS+=",JAVA_PARAMETERS="+JAVA_PARAMETERS
if(POD_MAX_MEM != "")
 OTHER_OSE3_TEMPLATE_PARAMS+=",POD_MAX_MEM="+POD_MAX_MEM
if(TZ != "")
 OTHER_OSE3_TEMPLATE_PARAMS+=",TZ="+TZ
if(WILY_MOM_FQDN != "")
 OTHER_OSE3_TEMPLATE_PARAMS+=",WILY_MOM_FQDN="+WILY_MOM_FQDN
if(WILY_MOM_PORT != "")
 OTHER_OSE3_TEMPLATE_PARAMS+=",WILY_MOM_PORT="+WILY_MOM_PORT



mavenJob (buildJobName) {
  println "JOB: "+buildJobName
  label('maven')
  deliveryPipelineConfiguration('CI', 'Build&Package')
  logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)

  parameters {
    // Defines a simple text parameter, where users can enter a string value.
    stringParam('gitlabActionType', 'PUSH',
                'GitLab Event (PUSH or MERGE)')
    stringParam('gitlabSourceRepoURL', GITLAB_SERVER+'/'+GITLAB_PROJECT+'.git',
                'GitLab Source Repository')
    stringParam('gitlabSourceRepoName', 'origin',
                'GitLab source repo name (only for MERGE events from forked repositories)')
    stringParam('gitlabSourceBranch', GIT_INTEGRATION_BRANCH,
                'Gitlab source branch (only for MERGE events from forked repositories)')
    stringParam('gitlabTargetBranch', GIT_INTEGRATION_BRANCH,
                'GitLab target branch (only for MERGE events)')
  }

  properties{
    promotions{
      promotion {
        name('Promote-pre')
        icon('star-gold-w')
        conditions {
          releaseBuild()
          manual('') {
          }
        }
        actions {
          downstreamParameterized {
            trigger(deployPreJobName,'SUCCESS') {
              parameters {
                predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
                predefinedProp('OSE3_CREDENTIAL', SERENITY_CREDENTIAL)
                predefinedProp('OSE3_APP_NAME',  APP_NAME_OSE3)
                predefinedProp('OSE3_TEMPLATE_NAME','javase')
                predefinedProp('VALUE_URL',nexusRepositoryUrl + '/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=releases')
              }
            }
          }
        }
      }
      promotion {
        name('DEV')
        icon('star-gold')
        conditions {
          downstream(false, deployDevJobName)
        }
      }
      promotion {
        name('PRE')
        icon('star-gold-w')
        conditions {
          downstream(false, deployPreJobName)
        }
      }
      promotion {
        name('PRO')
        icon('star-gold')
        conditions {
          downstream(false, deployProJobName)
        }
      }
    }
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
      wipeOutWorkspace(true)
    } //git
  } //scm

  triggers {
    gitlabPush {
      buildOnPushEvents(true)
      buildOnMergeRequestEvents(false)
      setBuildDescription(true)
      useCiFeatures(true)
      allowAllBranches(false)
      includeBranches(GIT_INTEGRATION_BRANCH)
    }
  } //triggers

  wrappers {
    credentialsBinding {
      usernamePassword('GITLAB_CREDENTIAL', SERENITY_CREDENTIAL)
    }
    buildName('${ENV,var="POM_DISPLAYNAME"}-${ENV,var="POM_VERSION"}-${BUILD_NUMBER}')
    release {
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","POM_GROUPID","POM_ARTIFACTID","POM_VERSION"]')
        }
      }
      configure {
        it / 'postSuccessfulBuildSteps' << 'hudson.plugins.git.GitPublisher'(plugin: 'git@2.4.1') {
          configVersion(2)
          pushMerge(false)
          pushOnlyIfSuccess(false)
          forcePush(false)
          tagsToPush {
            'hudson.plugins.git.GitPublisher_-TagToPush' {
              targetRepoName('origin')
              tagName('v${POM_VERSION}')
              tagMessage()
              createTag(false)
              updateTag(false)
            }
          }
          branchesToPush {
            'hudson.plugins.git.GitPublisher_-BranchToPush' {
              targetRepoName('origin')
              branchName(GIT_RELEASE_BRANCH)
            }
          }
        }
        postSuccessfulBuildSteps {
          shell("git checkout ${GIT_INTEGRATION_BRANCH}")

        }
        it / 'postSuccessfulBuildSteps' << 'hudson.plugins.git.GitPublisher'(plugin: 'git@2.4.1') {
          configVersion(2)
          pushMerge(false)
          pushOnlyIfSuccess(false)
          forcePush(false)
          branchesToPush {
            'hudson.plugins.git.GitPublisher_-BranchToPush' {
              targetRepoName('origin')
              branchName(GIT_INTEGRATION_BRANCH)
            }
          }
        }
        it / 'postSuccessfulBuildSteps' << 'hudson.maven.RedeployPublisher' {
          id('serenity')
          url(nexusRepositoryUrl+'/content/repositories/releases')
          uniqueVersion(true)
          evenIfUnstable(false)
        }
        it / 'preBuildSteps' << 'org.jenkinsci.plugins.configfiles.builder.ConfigFileBuildStep' (plugin: 'config-file-provider@2.10.0') {
            managedFiles {
              'org.jenkinsci.plugins.configfiles.buildwrapper.ManagedFile' {
                fileId('org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig')
                targetLocation('/tmp/settings.xml')
                variable('MAVEN_SETTINGS')
              }
            }
          }
        it / 'preBuildSteps' << 'hudson.tasks.Shell' {
          command("git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}")
        }
        it / 'preBuildSteps' << 'EnvInjectBuilder' (plugin: 'envinject@1.92.1') {
          info {
            propertiesContent('IS_RELEASE=true')
          }
        }
      }
    } //release
  }

  goals('clean verify')
    // Use managed global Maven settings.
  providedSettings('Serenity Maven Settings')
   mavenOpts('-Dmaven.wagon.http.ssl.insecure=true')
   mavenOpts('-Dmaven.wagon.http.ssl.allowall=true')
   mavenOpts('-Dmaven.wagon.http.ssl.ignore.validity.dates=true')
   
  publishers {
    deployArtifacts {
      repositoryId('serenity')
      repositoryUrl(nexusRepositoryUrl+'/content/repositories/snapshots')
      uniqueVersion(true)
    }
    flexiblePublish {
      conditionalAction{
            condition { not {
                    booleanCondition('${ENV,var="IS_RELEASE"}')
                      }
                }
             publishers {
                          downstreamParameterized {
                           trigger(deployDevJobName) {
                            condition('SUCCESS')
                             parameters {
                                predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-dev')
                                predefinedProp('OSE3_CREDENTIAL', SERENITY_CREDENTIAL)
                                predefinedProp('OSE3_APP_NAME', APP_NAME_OSE3)
                                predefinedProp('OSE3_TEMPLATE_NAME','javase')
                                predefinedProp('VALUE_URL',nexusRepositoryUrl + '/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=snapshots')
                               }
                              }
                           }
                        }
       } //conditionalAction
    } // flexiblePublish
    extendedEmail('$DEFAULT_RECIPIENTS', '$DEFAULT_SUBJECT', '${JELLY_SCRIPT, template="static-analysis.jelly"}') {
      trigger(triggerName: 'Always')
      trigger(triggerName: 'Failure', includeCulprits: true)
      trigger(triggerName: 'Unstable', includeCulprits: true)
      trigger(triggerName: 'FixedUnhealthy', sendToDevelopers: true)
      configure {
        it/contentType('text/html')
      }
    } //extendedEmail
  } //publishers

  configure {
    it/publishers/'hudson.maven.RedeployPublisher'/releaseEnvVar('IS_RELEASE')
  }
} //job

//Deploy in dev job
job (deployDevJobName) {
  println "JOB: " + deployDevJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('DEV', 'Deploy')
  parameters {
    stringParam('OSE3_APP_NAME', '', 'OSE3 application name')
    stringParam('OSE3_PROJECT_NAME', '', 'OSE3 project name')
    stringParam('OSE3_TEMPLATE_NAME', '', 'OSE3 template name')
    stringParam('VALUE_URL' , '', 'NEXUS URL ARTIFACT')
    credentialsParam('OSE3_CREDENTIAL') {
      type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
      required(false)
      defaultValue(SERENITY_CREDENTIAL)
      description('OSE3 credentials')
    }
    stringParam('PIPELINE_VERSION' , '', 'Pipeline version')
  }
  wrappers {
    credentialsBinding {
      usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', '${OSE3_CREDENTIAL}')
    }
    steps {
  	  shell(
  	    'export ARTIFACT_URL=$(curl -k -s -I $VALUE_URL -I | awk \'/Location: (.*)/ {print $2}\' | tail -n 1 | tr -d \'\\r\')\n' +
  	    'echo \"OSE3_TEMPLATE_PARAMS=APP_NAME=$OSE3_APP_NAME,ARTIFACT_URL=$ARTIFACT_URL'+ OTHER_OSE3_TEMPLATE_PARAMS + '\" > ${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties\n'
  	 ) 
  	  environmentVariables{
  	    propertiesFile('${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties')
  	  }
    shell('deploy_in_ose3.sh')
        environmentVariables
        {
          propertiesFile('${WORKSPACE}/deploy_jenkins.properties')
  	}
    }
  }
if(ADD_HPALM_AT_DEV == "true")
{
    publishers
    {
	downstreamParameterized {
	    trigger(BridgeHPALMJobNameDEV) {
        	condition('SUCCESS')
        	parameters {
         		 predefinedProp('OSE3_END_POINT_URL','${OSE3_END_POINT_URL}' )
		}
	    }
	}
    }
} //HPALM
}

//Use HPALM Bridge DEV 
if(ADD_HPALM_AT_DEV == "true")
{
job (BridgeHPALMJobNameDEV)
{
  println "JOB: ${BridgeHPALMJobNameDEV}"
    label("hpalm_bridge")
    parameters {
       stringParam('OSE3_END_POINT_URL', '', 'OS3 URL to be tested')
    }
    deliveryPipelineConfiguration('DEV', 'Functional Test (DEV)') 

    logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)
                
	scm {
    git {
      // Specify the branches to examine for changes and to build.
      branch(GIT_INTEGRATION_BRANCH)
      // Adds a repository browser for browsing the details of changes in an external system.
      browser {
if(GITLAB_PROJECT_TEST == "")
{
        gitLab(GITLAB_SERVER+'/'+GITLAB_PROJECT, '8.2')
}
else
{
        gitLab(GITLAB_SERVER+'/'+GITLAB_PROJECT_TEST, '8.2')
}
      } //browser
      // Adds a remote.
      remote {
        // Sets credentials for authentication with the remote repository.
        credentials(SERENITY_CREDENTIAL)
        // Sets a name for the remote.
        name('origin')
        // Sets the remote URL.
if(GITLAB_PROJECT_TEST == "")
{
        url(GITLAB_SERVER+'/'+GITLAB_PROJECT+'.git')
}
else
{
        url(GITLAB_SERVER+'/'+GITLAB_PROJECT_TEST+'.git')
}
      } //remote
      wipeOutWorkspace(true)
    } //git
	} //scm
	
  	wrappers {
        credentialsBinding {
            usernamePassword('CREDENTIALS', _HPALM_CREDS_)
        }
    }
	 steps {
        environmentVariables
        {
	  if(URL_BASE_SELENIUM == "")
	  {
	    env('seleniumBaseURL','${OSE3_END_POINT_URL}')
	  }
	  else
	  {
	    env('seleniumBaseURL','${URL_BASE_SELENIUM}')
	  }
  	}
	   maven {
	    goals('test')
  		providedSettings('Serenity Maven Settings')
        rootPOM(_POM_PATH_) 
		mavenOpts('-Dhpalm.test.set.id='+_HPALM_TEST_SET_ID_)
		mavenOpts('-Dhpalm.domain='+_HPALM_DOMAIN_)
		mavenOpts('-Dhpalm.project='+_HPALM_PROJECT_)
        	mavenOpts('-Dmaven.test.failure.ignore=true')
	  }
	  
      shell(
		'#!/bin/bash\n'+
		'/tmp/hpalm-bridge.sh ' + _HPALM_URL_ + ' \"' + _TEST_RESULT_PATH_ +'\"'
	  )	
	  }//steps
}
}//HPALM BRIDGE DEV

//HPALM Bridge PRE
if(ADD_HPALM_AT_PRE == "true")
{
job (BridgeHPALMJobName)
{
  println "JOB: ${BridgeHPALMJobName}"
    label("hpalm_bridge")
    parameters {
       stringParam('OSE3_END_POINT_URL', '', 'OS3 URL to be tested')
    }
    deliveryPipelineConfiguration('PRE', 'Functional Test') 

    logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)
                
	scm {
    git {
      // Specify the branches to examine for changes and to build.
      branch(GIT_INTEGRATION_BRANCH)
      // Adds a repository browser for browsing the details of changes in an external system.
      browser {
if(GITLAB_PROJECT_TEST == "")
{
        gitLab(GITLAB_SERVER+'/'+GITLAB_PROJECT, '8.2')
}
else
{
        gitLab(GITLAB_SERVER+'/'+GITLAB_PROJECT_TEST, '8.2')
}
      } //browser
      // Adds a remote.
      remote {
        // Sets credentials for authentication with the remote repository.
        credentials(SERENITY_CREDENTIAL)
        // Sets a name for the remote.
        name('origin')
        // Sets the remote URL.
if(GITLAB_PROJECT_TEST == "")
{
        url(GITLAB_SERVER+'/'+GITLAB_PROJECT+'.git')
}
else
{
        url(GITLAB_SERVER+'/'+GITLAB_PROJECT_TEST+'.git')
}
      } //remote
      wipeOutWorkspace(true)
    } //git
	} //scm
	
  	wrappers {
        credentialsBinding {
            usernamePassword('CREDENTIALS', _HPALM_CREDS_)
        }
    }
	 steps {
        environmentVariables
        {
	  if(URL_BASE_SELENIUM == "")
	  {
	    env('seleniumBaseURL','${OSE3_END_POINT_URL}')
	  }
	  else
	  {
	    env('seleniumBaseURL','${URL_BASE_SELENIUM}')
	  }
  	}
	   maven {
	    goals('test')
  		providedSettings('Serenity Maven Settings')
        rootPOM(_POM_PATH_) 
		mavenOpts('-Dhpalm.test.set.id='+_HPALM_TEST_SET_ID_)
		mavenOpts('-Dhpalm.domain='+_HPALM_DOMAIN_)
		mavenOpts('-Dhpalm.project='+_HPALM_PROJECT_)
                mavenOpts('-Dmaven.test.failure.ignore=true')
	  }
	  
      shell(
		'#!/bin/bash\n'+
		'/tmp/hpalm-bridge.sh ' + _HPALM_URL_ + ' \"' + _TEST_RESULT_PATH_ +'\"'
	  )	
	  }//steps
}
}//HPALM BRIDGE PRE

//Deploy in pre job
job (deployPreJobName) {
  println "JOB: " + deployPreJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('PRE', 'Deploy')
  parameters {
    stringParam('OSE3_APP_NAME', '', 'OSE3 application name')
    stringParam('OSE3_PROJECT_NAME', '', 'OSE3 project name')
    stringParam('OSE3_TEMPLATE_NAME', '', 'OSE3 template name')
    stringParam('VALUE_URL' , '', 'NEXUS URL ARTIFACT')
    credentialsParam('OSE3_CREDENTIAL') {
      type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
      required(false)
      defaultValue(SERENITY_CREDENTIAL)
      description('OSE3 credentials')
    }
    stringParam('PIPELINE_VERSION' , '', 'Pipeline version')
  }
  wrappers {
    credentialsBinding {
      usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', '${OSE3_CREDENTIAL}')
    }
  }
  properties {
    promotions {
     promotion {
       name('Promote-PRO')
       icon('star-gold-e')
         conditions {
           manual('') {}
         }
         actions {
           downstreamParameterized {
             trigger(deployProJobName, 'SUCCESS') {
               parameters {
                 predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pro')
                 predefinedProp('OSE3_CREDENTIAL', '${OSE3_CREDENTIAL}')
                 predefinedProp('OSE3_APP_NAME', '${OSE3_APP_NAME}')
                 predefinedProp('OSE3_TEMPLATE_NAME','${OSE3_TEMPLATE_NAME}')
                 predefinedProp('OSE3_TEMPLATE_PARAMS','${OSE3_TEMPLATE_PARAMS}')
               }
             }
           }
         }
       }
     }
   }
  steps {
  	  shell(
  	    'export ARTIFACT_URL=$(curl -k -s -I $VALUE_URL -I | awk \'/Location: (.*)/ {print $2}\' | tail -n 1 | tr -d \'\\r\')\n' +
  	    'echo \"OSE3_TEMPLATE_PARAMS=APP_NAME=$OSE3_APP_NAME,ARTIFACT_URL=$ARTIFACT_URL'+ OTHER_OSE3_TEMPLATE_PARAMS + '\" > ${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties\n'
  	 ) 
  	  environmentVariables{
  	    propertiesFile('${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties')
  	  }
    shell('deploy_in_ose3.sh')
        environmentVariables
        {
          propertiesFile('${WORKSPACE}/deploy_jenkins.properties')
  	}
    }
if( ADD_HPALM_AT_PRE == "true")
{
    publishers
    {
	downstreamParameterized {
	    trigger(BridgeHPALMJobName) {
        	condition('SUCCESS')
        	parameters {
         		 predefinedProp('OSE3_END_POINT_URL','${OSE3_END_POINT_URL}' )
		}
	    }
	}
    }
} //HPALM
}

//Deploy in pro job
job (deployProJobName) {
  println "JOB: " + deployProJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('PRO', 'Deploy')
  parameters {
    stringParam('OSE3_APP_NAME', '', 'OSE3 application name')
    stringParam('OSE3_PROJECT_NAME', '', 'OSE3 project name')
    stringParam('OSE3_TEMPLATE_NAME', '', 'OSE3 template name')
    stringParam('OSE3_TEMPLATE_PARAMS' , '', 'OSE3 template params')
    credentialsParam('OSE3_CREDENTIAL') {
      type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
      required(false)
      defaultValue(SERENITY_CREDENTIAL)
      description('OSE3 credentials')
    }
    stringParam('PIPELINE_VERSION' , '', 'Pipeline version')
  }
  wrappers {
    credentialsBinding {
      usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', '${OSE3_CREDENTIAL}')
    }
  }
  steps {
    shell('deploy_in_ose3.sh')
  }
}
