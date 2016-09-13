import jenkins.model.*
import java.util.regex.*;
import util.Utilities;

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))
def utils = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/Utils.groovy"))

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_INTEGRATION_BRANCH_FEATURE_A = "${GIT_INTEGRATION_BRANCH_FEATURE_A}".trim()
def GIT_INTEGRATION_BRANCH_FEATURE_B = "${GIT_INTEGRATION_BRANCH_FEATURE_B}".trim()
def GIT_RELEASE_BRANCH_FEATURE_A = "${GIT_RELEASE_BRANCH_FEATURE_A}".trim()
def GIT_RELEASE_BRANCH_FEATURE_B = "${GIT_RELEASE_BRANCH_FEATURE_B}".trim()
def GITLAB_CREDENTIAL = "${GITLAB_CREDENTIAL}"
def OSE3_URL ="${OSE3_URL}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim()

// Static values
//checks gitlab url
def gitLabMap = Utilities.parseGitlabUrl(GITLAB_PROJECT);
def GROUP_NAME = gitLabMap.groupName
def REPOSITORY_NAME = gitLabMap.repositoryName
def GITLAB_URL = gitLabMap.url

def GITLAB_SERVER = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger").getGitlabHostUrl();
def GITLAB_API_TOKEN = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger").getGitlabApiToken();
out.println("GitLab URL: " + GITLAB_URL);
out.println("GitLab Group: " + GROUP_NAME);
out.println("GitLab Project: " + REPOSITORY_NAME);
GITLAB_PROJECT = GROUP_NAME + '/' + REPOSITORY_NAME

def buildJobName_a = GITLAB_PROJECT+'-ci-build-feature-A'
def buildJobName_b = GITLAB_PROJECT+'-ci-build-feature-B'
def deployDevJobName = GITLAB_PROJECT+'-ose3-dev-deploy'
def deployPreJobName = GITLAB_PROJECT+'-ose3-pre-deploy'
def deployProJobName = GITLAB_PROJECT+'-ose3-pro-deploy'
def nexusRepositoryUrl = System.getenv('NEXUS_BASE_URL') ?: 'https://nexus.ci.gsnet.corp/nexus'
def mavenGroupRepository = System.getenv('NEXUS_MAVEN_GROUP') ?: '/content/groups/public/'
def mavenReleaseRepository = System.getenv('NEXUS_MAVEN_RELEASES' ?: '/content/repositories/releases/'
def mavenSnapshotRepository = System.getenv('NEXUS_MAVEN_SNAPSHOTS') ?: '/content/repositories/snapshots/'

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
def APP_NAME_OSE3_FEATURE_A="${APP_NAME_OSE3_FEATURE_A}".trim().toLowerCase()
def APP_NAME_OSE3_FEATURE_B="${APP_NAME_OSE3_FEATURE_B}".trim().toLowerCase()

//TOKEN_OSE3
def OSE3_TOKEN_PROJECT_DEV="${OSE3_TOKEN_PROJECT_DEV}".trim()
def OSE3_TOKEN_PROJECT_PRE=""
def OSE3_TOKEN_PROJECT_PRO=""


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


//creck gitlab credentials
def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL)
if ( gitlabCredsType == null ) {
  throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
}
println ("GitLab credential type " + gitlabCredsType );

mavenJob (buildJobName_a) {
  println "JOB: "+buildJobName_a
  label('maven')
  deliveryPipelineConfiguration('CI', 'Build&Package')
  logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)

  parameters {
    // Defines a simple text parameter, where users can enter a string value.
    stringParam('gitlabActionType', 'PUSH',
                'GitLab Event (PUSH or MERGE)')
    stringParam('gitlabSourceRepoURL', GITLAB_URL+GITLAB_PROJECT+'.git',
                'GitLab Source Repository')
    stringParam('gitlabSourceRepoName', 'origin',
                'GitLab source repo name (only for MERGE events from forked repositories)')
    stringParam('gitlabSourceBranch', GIT_INTEGRATION_BRANCH_FEATURE_A,
                'Gitlab source branch (only for MERGE events from forked repositories)')
    stringParam('gitlabTargetBranch', GIT_INTEGRATION_BRANCH_FEATURE_A,
                'GitLab target branch (only for MERGE events)')
  } //parameters

  properties{
    promotions{
      promotion {
        name('Promote-pre')
        icon('star-gold-w')
        conditions {
          releaseBuild()
          manual('impes-product-owner,impes-technical-lead,impes-developer')
        }
        actions {
          downstreamParameterized {
            trigger(deployPreJobName) {
              parameters {
                predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
                predefinedProp('OSE3_APP_NAME',  APP_NAME_OSE3_FEATURE_A)
                predefinedProp('OSE3_TEMPLATE_NAME','javase-ab')
                predefinedProp('OSE3_URL', OSE3_URL)
                predefinedProp('OSE3_APP_VERSION', '${POM_VERSION}')
                predefinedProp('VALUE_URL',nexusRepositoryUrl + '/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=releases')
              }
            }
          }
        } //actions
      } //promotion
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
    } //promotions
  } //properties

  scm {
    git {
      // Specify the branches to examine for changes and to build.
      branch('${gitlabSourceRepoName}/${gitlabSourceBranch}')
      // Adds a repository browser for browsing the details of changes in an external system.
      browser {
        gitLab(GITLAB_SERVER+GITLAB_PROJECT, '8.2')
      } //browser
      // Adds a remote.
      remote {
        // Sets credentials for authentication with the remote repository.
        credentials(GITLAB_CREDENTIAL)
        // Sets a name for the remote.
        name('origin')
        // Sets the remote URL.
        url(GITLAB_URL+GITLAB_PROJECT+'.git')
      } //remote
      extensions {
        wipeOutWorkspace()
      }
    } //git
  } //scm

  triggers {
    gitlabPush {
      buildOnPushEvents(true)
      buildOnMergeRequestEvents(false)
      setBuildDescription(true)
      useCiFeatures(true)
      allowAllBranches(false)
      includeBranches(GIT_INTEGRATION_BRANCH_FEATURE_A)
    }
  } //triggers

  wrappers {
    credentialsBinding {
//If user password credentials are provided bind is required
if ( gitlabCredsType == 'UserPassword' ){
          usernamePassword('GITLAB_CREDENTIAL', GITLAB_CREDENTIAL)
}
     }
//if ssh credentials ssAgent is added
if ( gitlabCredsType == 'SSH' ){
      sshAgent(GITLAB_CREDENTIAL)
}

    buildName('${ENV,var="POM_DISPLAYNAME"}-${ENV,var="POM_VERSION"}-${BUILD_NUMBER}')
    release {
      preBuildSteps {
        environmentVariables {
          env('IS_RELEASE', 'true')
        }
      }
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","POM_GROUPID","POM_ARTIFACTID","POM_VERSION"]')
        }
      }
      postSuccessfulBuildSteps {
        shell("git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH_FEATURE_A} ${GIT_RELEASE_BRANCH_FEATURE_A}")
      }
      configure {
        it / 'postSuccessfulBuildSteps' << 'hudson.maven.RedeployPublisher' {
          id('serenity')
          url(nexusRepositoryUrl+mavenReleaseRepository)
          uniqueVersion(true)
          evenIfUnstable(false)
        }
        it / 'preBuildSteps' << 'org.jenkinsci.plugins.configfiles.builder.ConfigFileBuildStep' (plugin: 'config-file-provider@2.10.0') {
          managedFiles {
            'org.jenkinsci.plugins.configfiles.buildwrapper.ManagedFile' {
              fileId('org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig')
              variable('MAVEN_SETTINGS')
            }
          }
        }
      }
    } //release
  }

  // Fix buildWrappers order issue moving preBuildSteps out of release
  preBuildSteps {
    shell("if [ \"\${IS_RELEASE}\" = true ]; then git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH_FEATURE_A} ${GIT_RELEASE_BRANCH_FEATURE_A}; fi")
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
      repositoryUrl(nexusRepositoryUrl+mavenSnapshotRepository')
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
                                predefinedProp('OSE3_APP_NAME', APP_NAME_OSE3_FEATURE_A)
                                predefinedProp('OSE3_URL', OSE3_URL)
                                predefinedProp('OSE3_APP_VERSION', '${POM_VERSION}')
                                predefinedProp('OSE3_TEMPLATE_NAME','javase-ab')
                                predefinedProp('VALUE_URL',nexusRepositoryUrl + '/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=snapshots')
                               }
                              }
                           }
                        }
       } //conditionalAction
    } // flexiblePublish
    extendedEmail {
      defaultContent('${JELLY_SCRIPT, template="static-analysis.jelly"}')
      contentType('text/html')
      triggers {
        always()
        failure {
          sendTo {
            culprits()
          }
        }
        unstable {
          sendTo {
            culprits()
          }
        }
        fixedUnhealthy {
          sendTo {
            developers()
          }
        }
      }
    } //extendedEmail
  } //publishers

  configure {
    it/publishers/'hudson.maven.RedeployPublisher'/releaseEnvVar('IS_RELEASE')
  }
} //job build - feature A


mavenJob (buildJobName_b) {
  println "JOB: "+buildJobName_b
  label('maven')
  deliveryPipelineConfiguration('CI', 'Build&Package')
  logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)

  parameters {
    // Defines a simple text parameter, where users can enter a string value.
    stringParam('gitlabActionType', 'PUSH',
                'GitLab Event (PUSH or MERGE)')
    stringParam('gitlabSourceRepoURL', GITLAB_URL+GITLAB_PROJECT+'.git',
                'GitLab Source Repository')
    stringParam('gitlabSourceRepoName', 'origin',
                'GitLab source repo name (only for MERGE events from forked repositories)')
    stringParam('gitlabSourceBranch', GIT_INTEGRATION_BRANCH_FEATURE_B,
                'Gitlab source branch (only for MERGE events from forked repositories)')
    stringParam('gitlabTargetBranch', GIT_INTEGRATION_BRANCH_FEATURE_B,
                'GitLab target branch (only for MERGE events)')
  } //parameters

  properties{
    promotions{
      promotion {
        name('Promote-pre')
        icon('star-gold-w')
        conditions {
          releaseBuild()
          manual('impes-product-owner,impes-technical-lead,impes-developer')
        }
        actions {
          downstreamParameterized {
            trigger(deployPreJobName) {
              parameters {
                predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
                predefinedProp('OSE3_APP_NAME',  APP_NAME_OSE3_FEATURE_B)
                predefinedProp('OSE3_TEMPLATE_NAME','javase-ab')
                predefinedProp('OSE3_APP_VERSION', '${POM_VERSION}')
                predefinedProp('OSE3_URL', OSE3_URL)
                predefinedProp('VALUE_URL',nexusRepositoryUrl + '/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=releases')
              }
            }
          }
        } //actions
      } //promotion
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
    } //promotions
  } //properties

  scm {
    git {
      // Specify the branches to examine for changes and to build.
      branch('${gitlabSourceRepoName}/${gitlabSourceBranch}')
      // Adds a repository browser for browsing the details of changes in an external system.
      browser {
        gitLab(GITLAB_SERVER+GITLAB_PROJECT, '8.2')
      } //browser
      // Adds a remote.
      remote {
        // Sets credentials for authentication with the remote repository.
        credentials(GITLAB_CREDENTIAL)
        // Sets a name for the remote.
        name('origin')
        // Sets the remote URL.
        url(GITLAB_URL+GITLAB_PROJECT+'.git')
      } //remote
      extensions {
        wipeOutWorkspace()
      }
    } //git
  } //scm

  triggers {
    gitlabPush {
      buildOnPushEvents(true)
      buildOnMergeRequestEvents(false)
      setBuildDescription(true)
      useCiFeatures(true)
      allowAllBranches(false)
      includeBranches(GIT_INTEGRATION_BRANCH_FEATURE_B)
    }
  } //triggers

  wrappers {
    credentialsBinding {
//If user password credentials are provided bind is required
if ( gitlabCredsType == 'UserPassword' ){
          usernamePassword('GITLAB_CREDENTIAL', GITLAB_CREDENTIAL)
}
     }
//if ssh credentials ssAgent is added
if ( gitlabCredsType == 'SSH' ){
      sshAgent(GITLAB_CREDENTIAL)
}
    buildName('${ENV,var="POM_DISPLAYNAME"}-${ENV,var="POM_VERSION"}-${BUILD_NUMBER}')
    release {
      preBuildSteps {
        environmentVariables {
          env('IS_RELEASE', 'true')
        }
      }
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","POM_GROUPID","POM_ARTIFACTID","POM_VERSION"]')
        }
      }
      postSuccessfulBuildSteps {
        shell("git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH_FEATURE_B} ${GIT_RELEASE_BRANCH_FEATURE_B}")
      }
      configure {
        it / 'postSuccessfulBuildSteps' << 'hudson.maven.RedeployPublisher' {
          id('serenity')
          url(nexusRepositoryUrl+mavenReleaseRepository)
          uniqueVersion(true)
          evenIfUnstable(false)
        }
        it / 'preBuildSteps' << 'org.jenkinsci.plugins.configfiles.builder.ConfigFileBuildStep' (plugin: 'config-file-provider@2.10.0') {
          managedFiles {
            'org.jenkinsci.plugins.configfiles.buildwrapper.ManagedFile' {
              fileId('org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig')
              variable('MAVEN_SETTINGS')
            }
          }
        }
      }
    } //release
  }

  // Fix buildWrappers order issue moving preBuildSteps out of release
  preBuildSteps {
    shell("if [ \"\${IS_RELEASE}\" = true ]; then git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH_FEATURE_B} ${GIT_RELEASE_BRANCH_FEATURE_B}; fi")
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
      repositoryUrl(nexusRepositoryUrl+mavenSnapshotRepository')
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
                                predefinedProp('OSE3_APP_NAME', APP_NAME_OSE3_FEATURE_B)
                                predefinedProp('OSE3_URL', OSE3_URL) 
                                predefinedProp('OSE3_APP_VERSION', '${POM_VERSION}')
                                predefinedProp('OSE3_TEMPLATE_NAME','javase-ab')
                                predefinedProp('VALUE_URL',nexusRepositoryUrl + '/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=snapshots')
                               }
                              }
                           }
                        }
       } //conditionalAction
    } // flexiblePublish
    extendedEmail {
      defaultContent('${JELLY_SCRIPT, template="static-analysis.jelly"}')
      contentType('text/html')
      triggers {
        always()
        failure {
          sendTo {
            culprits()
          }
        }
        unstable {
          sendTo {
            culprits()
          }
        }
        fixedUnhealthy {
          sendTo {
            developers()
          }
        }
      }
    } //extendedEmail
  } //publishers

  configure {
    it/publishers/'hudson.maven.RedeployPublisher'/releaseEnvVar('IS_RELEASE')
  }
} //job build feature-B


//Deploy in dev job
job (deployDevJobName) {
  println "JOB: " + deployDevJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('DEV', 'Deploy')
  parameters {
    stringParam('OSE3_APP_NAME', '', 'OSE3 application name')
    stringParam('OSE3_PROJECT_NAME', '', 'OSE3 project name')
    stringParam('OSE3_TEMPLATE_NAME', '', 'OSE3 template name')
    stringParam('OSE3_URL', '', 'OSE3 URL')
    stringParam('OSE3_APP_VERSION', '${POM_VERSION}')
    stringParam('VALUE_URL' , '', 'NEXUS URL ARTIFACT')
    //credentialsParam('OSE3_CREDENTIAL') {
    //  type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
    //  required(false)
    //  defaultValue(SERENITY_CREDENTIAL)
    //  description('OSE3 credentials')
    //}
    stringParam('PIPELINE_VERSION' , '', 'Pipeline version')
  }
   configure {
               it / 'properties' / 'hudson.model.ParametersDefinitionProperty' / parameterDefinitions << 'hudson.model.PasswordParameterDefinition' {
               name 'OSE3_TOKEN_PROJECT'
               description 'OSE3 token project'
               defaultValue OSE3_TOKEN_PROJECT_DEV
      }
  }
  
 
  wrappers {
   // credentialsBinding {
   //   usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', '${OSE3_CREDENTIAL}')
   // }
    steps {
  	  shell(
  	    'export ARTIFACT_URL=$(curl -k -s -I $VALUE_URL -I | awk \'/Location: (.*)/ {print $2}\' | tail -n 1 | tr -d \'\\r\')\n' +
  	    'echo \"OSE3_TEMPLATE_PARAMS=APP_VERSION=$OSE3_APP_VERSION,APP_NAME=$OSE3_APP_NAME,ARTIFACT_URL=$ARTIFACT_URL'+ OTHER_OSE3_TEMPLATE_PARAMS + '\" > ${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties\n'
  	 ) 
  	  environmentVariables{
  	    propertiesFile('${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties')
  	  }
    shell('deploy_in_ose3.sh --ab_testing=ON --create_template=ON --login_with_token=ON')
        environmentVariables
        {
          propertiesFile('${WORKSPACE}/deploy_jenkins.properties')
    	}
    }
    //systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/UpdateLinkAction.groovy')) {
    //  binding('LINK_URL', 'OSE3_END_POINT_URL')
    //}
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
        gitLab(GITLAB_SERVER+GITLAB_PROJECT, '8.2')
}
else
{
        gitLab(GITLAB_SERVER+GITLAB_PROJECT_TEST, '8.2')
}
      } //browser
      // Adds a remote.
      remote {
        // Sets credentials for authentication with the remote repository.
        credentials(GITLAB_CREDENTIAL)
        // Sets a name for the remote.
        name('origin')
        // Sets the remote URL.
if(GITLAB_PROJECT_TEST == "")
{
        url(GITLAB_URL+GITLAB_PROJECT+'.git')
}
else
{
        url(GITLAB_URL+GITLAB_PROJECT_TEST+'.git')
}
      } //remote
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
        gitLab(GITLAB_SERVER+GITLAB_PROJECT, '8.2')
}
else
{
        gitLab(GITLAB_SERVER+GITLAB_PROJECT_TEST, '8.2')
}
      } //browser
      // Adds a remote.
      remote {
        // Sets credentials for authentication with the remote repository.
        credentials(GITLAB_CREDENTIAL)
        // Sets a name for the remote.
        name('origin')
        // Sets the remote URL.
if(GITLAB_PROJECT_TEST == "")
{
        url(GITLAB_URL+GITLAB_PROJECT+'.git')
}
else
{
        url(GITLAB_URL+GITLAB_PROJECT_TEST+'.git')
}
      } //remote
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

def injectPasswords = {
  it / buildWrappers / EnvInjectPasswordWrapper(plugin:"envinject@1.92.1") {
    injectGlobalPasswords(false)
    maskPasswordParameters(true)
    passwordEntries {
      EnvInjectPasswordEntry {
        name('OSE3_USERNAME')
        value('CzYyIJFnWUx1/xdbbBfd4g==')
      }
      EnvInjectPasswordEntry {
        name('OSE3_PASSWORD')
        value('CzYyIJFnWUx1/xdbbBfd4g==')
      }
    }
  }
}

//Deploy in pre job
job (deployPreJobName) {
  println "JOB: " + deployPreJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('PRE', 'Deploy')
  parameters {
    stringParam('OSE3_APP_NAME', '', 'OSE3 application name')
    stringParam('OSE3_PROJECT_NAME', '', 'OSE3 project name')
    stringParam('OSE3_TEMPLATE_NAME', '', 'OSE3 template name')
    stringParam('OSE3_APP_VERSION', '${POM_VERSION}')
    stringParam('OSE3_URL' , '', 'OSE3_URL')
    stringParam('VALUE_URL' , '', 'NEXUS URL ARTIFACT')
    stringParam('PIPELINE_VERSION' , '', 'Pipeline version')
  }
   configure {
               it / 'properties' / 'hudson.model.ParametersDefinitionProperty' / parameterDefinitions << 'hudson.model.PasswordParameterDefinition' {
               name 'OSE3_TOKEN_PROJECT'
               description 'OSE3 token project'
               defaultValue OSE3_TOKEN_PROJECT_PRE
      }
  }

  properties {
    promotions {
     promotion {
       name('Promote-PRO')
       icon('star-gold-e')
         conditions {
           manual('impes-product-owner,impes-technical-lead,impes-developer')
         }
         actions {
           downstreamParameterized {
             trigger(deployProJobName) {
               parameters {
                 predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pro')
                 predefinedProp('OSE3_APP_NAME', '${OSE3_APP_NAME}')
                 predefinedProp('OSE3_TEMPLATE_NAME','${OSE3_TEMPLATE_NAME}')
                 predefinedProp('OSE3_APP_VERSION','${OSE3_APP_VERSION}')
                 predefinedProp('OSE3_URL',OSE3_URL)
                 predefinedProp('VALUE_URL','${VALUE_URL}')
               }
             }
           }
         }
       }
     }
   }
  //configure injectPasswords
  steps {
    
	shell(
            'export ARTIFACT_URL=$(curl -k -s -I $VALUE_URL -I | awk \'/Location: (.*)/ {print $2}\' | tail -n 1 | tr -d \'\\r\')\n' +
            'echo \"OSE3_TEMPLATE_PARAMS=APP_VERSION=$OSE3_APP_VERSION,APP_NAME=$OSE3_APP_NAME,ARTIFACT_URL=$ARTIFACT_URL'+ OTHER_OSE3_TEMPLATE_PARAMS + '\" > ${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties\n'
         )
          environmentVariables{
            propertiesFile('${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties')
          }
   shell('deploy_in_ose3.sh --ab_testing=ON --create_template=ON --login_with_token=ON')
   //     environmentVariables
   //     {
   //       propertiesFile('${WORKSPACE}/deploy_jenkins.properties')
   //	}
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
    stringParam('OSE3_APP_VERSION', '')
    stringParam('OSE3_URL' , '', 'OSE3 URL')
    stringParam('VALUE_URL' , '', 'NEXUS URL ARTIFACT')
    stringParam('PIPELINE_VERSION' , '', 'Pipeline version')
  }
   configure {
               it / 'properties' / 'hudson.model.ParametersDefinitionProperty' / parameterDefinitions << 'hudson.model.PasswordParameterDefinition' {
               name 'OSE3_TOKEN_PROJECT'
               description 'OSE3 token project'
               defaultValue OSE3_TOKEN_PROJECT_PRO
      }
  }


  //configure injectPasswords
  steps {
       shell(
            'export ARTIFACT_URL=$(curl -k -s -I $VALUE_URL -I | awk \'/Location: (.*)/ {print $2}\' | tail -n 1 | tr -d \'\\r\')\n' +
            'echo \"OSE3_TEMPLATE_PARAMS=APP_VERSION=$OSE3_APP_VERSION,APP_NAME=$OSE3_APP_NAME,ARTIFACT_URL=$ARTIFACT_URL'+ OTHER_OSE3_TEMPLATE_PARAMS + '\" > ${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties\n'
         )
          environmentVariables{
            propertiesFile('${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties')
          }

    shell('deploy_in_ose3.sh --ab_testing=ON --create_template=ON --login_with_token=ON')
  }
}

gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, buildJobName_a)
gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, buildJobName_b)
