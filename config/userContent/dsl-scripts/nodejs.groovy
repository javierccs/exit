import jenkins.model.*
import groovy.util.*
import java.util.regex.*;
import util.Utilities;

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))
def utils = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/Utils.groovy"))

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_INTEGRATION_BRANCH = "${GIT_INTEGRATION_BRANCH}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def OSE3_URL ="${OSE3_URL}".trim()
def OSE3_APP_NAME="${OSE3_APP_NAME}".trim().toLowerCase()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim().toLowerCase()
def GITLAB_CREDENTIAL = "${GITLAB_CREDENTIAL}"
def SERENITY_CREDENTIAL = "${SERENITY_CREDENTIAL}"

// Static values
//checks gitlab url
def gitLabMap = Utilities.parseGitlabUrl(GITLAB_PROJECT);
def GROUP_NAME = gitLabMap.groupName
def REPOSITORY_NAME = gitLabMap.repositoryName
def GITLAB_URL = gitLabMap.url
def gitLabConnectionMap = Utilities.getGitLabConnection ("Serenity GitLab")
def GITLAB_SERVER = gitLabConnectionMap.url;
def GITLAB_API_TOKEN = gitLabConnectionMap.credential.getApiToken().toString();
out.println("GitLab URL: " + GITLAB_URL);
out.println("GitLab Group: " + GROUP_NAME);
out.println("GitLab Project: " + REPOSITORY_NAME);

GITLAB_PROJECT = GROUP_NAME + '/' + REPOSITORY_NAME

def buildJobName = GITLAB_PROJECT+'-ci-build'
def dockerJobName = GITLAB_PROJECT+'-ci-docker'
def deployDevJobName = GITLAB_PROJECT+'-ose3-dev-deploy'
def deployPreJobName = GITLAB_PROJECT+'-ose3-pre-deploy'
def deployHideJobName = GITLAB_PROJECT+'-ose3-pro-deploy-shadow'
def deployProJobName = GITLAB_PROJECT+'-ose3-pro-route-switch'


def COMPILER = "${COMPILER}".trim()
def CONFIG_DIRECTORY = "${CONFIG_DIRECTORY}".trim()

//JAVASE TEMPLATE VARS
def OSE3_TEMPLATE_PARAMS ="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/"+GITLAB_PROJECT.toLowerCase()+':${FRONT_IMAGE_VERSION}'
// JAVA_OPTS_EXT="${JAVA_OPTS_EXT}".trim()
def TZ="${TZ}".trim()
def DIST_DIR="${DIST_DIR}".trim()
def DIST_INCLUDE="${DIST_INCLUDE}".trim()
def DIST_EXCLUDE="${DIST_EXCLUDE}".trim()
def JUNIT_TESTS_PATTERN="${JUNIT_TESTS_PATTERN}".trim()
//Compose the template params, if blank we left the default pf PAAS
if(TZ != "") OSE3_TEMPLATE_PARAMS+="TZ="+TZ

//SONARQUBE
String NAME="Serenity SonarQube"
//def sqd = Jenkins.getInstance().getDescriptor("hudson.plugins.sonar.SonarPublisher")
//boolean sq = (sqd != null) && sqd.getInstallations().find {NAME.equals(it.getName())}

//creck gitlab credentials
def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL)
if ( gitlabCredsType == null ) {
  throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
}
println ("GitLab credential type " + gitlabCredsType );

//TOKEN_OSE3
def OSE3_TOKEN_PROJECT_DEV="${OSE3_TOKEN_PROJECT_DEV}".trim()
def OSE3_TOKEN_PROJECT_PRE=""
def OSE3_TOKEN_PROJECT_PRO=""

job (buildJobName) {
  println "JOB: "+buildJobName
  label('nodejs')
  deliveryPipelineConfiguration('CI', 'Build')
  logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)

  parameters {
    // Defines a simple text parameter, where users can enter a string value.
    stringParam('gitlabActionType', 'PUSH',
                'GitLab Event (PUSH or MERGE)')
    stringParam('gitlabSourceRepoURL', GITLAB_URL+GITLAB_PROJECT+'.git',
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
        icon('star-silver-w')
        conditions {
          releaseBuild()
          manual('impes-product-owner,impes-technical-lead,impes-developer')
        }
        actions {
          downstreamParameterized {
            trigger(deployPreJobName) {
              parameters {
                predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS}")
                predefinedProp('PIPELINE_VERSION','${FRONT_IMAGE_VERSION}')
              }
            }
          }
        }
      }
      promotion {
        name('DEV')
        icon('star-blue')
        conditions {
          downstream(false, deployDevJobName)
        }
      }
      promotion {
        name('PRE')
        icon('star-silver-w')
        conditions {
          downstream(false, deployPreJobName)
        }
      }
      promotion {
        name('Shadow')
        icon('star-gold-w')
        conditions {
          downstream(false, deployHideJobName)
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
      branch('${gitlabSourceRepoName}/${gitlabSourceBranch}')
      browser {
        gitLab(GITLAB_SERVER+GITLAB_PROJECT, '8.6')
      } //browser
      remote {
        credentials(GITLAB_CREDENTIAL)
        name('origin')
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
      includeBranches(GIT_INTEGRATION_BRANCH)
    }
  } //triggers

  wrappers {
	preBuildCleanup()
        credentialsBinding {
//If user password credentials are provided bind is required
if ( gitlabCredsType == 'UserPassword' ){
          usernamePassword('GITLAB_CREDENTIAL', GITLAB_CREDENTIAL)
}
      //adds ose3 credentials
       usernamePassword('OSE3_USERNAME','OSE3_PASSWORD', SERENITY_CREDENTIAL)
     }
//if ssh credentials ssAgent is added
if ( gitlabCredsType == 'SSH' ){
      sshAgent(GITLAB_CREDENTIAL)
}

	buildName( REPOSITORY_NAME + ':${ENV,var="FRONT_IMAGE_VERSION"}')
    release {
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","FRONT_IMAGE_VERSION"]')
        }
      }
      postSuccessfulBuildSteps {
//If No compilation application.yaml versioning is used
if ( COMPILER.equals ( "None" )) {
        shell ( "application_yaml_git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH} ")
} else {
	    shell ( "git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}")
}
			  
      }
      preBuildSteps {
        environmentVariables {
          env('IS_RELEASE',true)
        }
      }
    } //release
  }
  steps {
if ( COMPILER.equals ( "None" )) {
    shell("if [ \"\${IS_RELEASE}\" = true ]; then application_yaml_git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}; fi")
} else {	
    shell("if [ \"\${IS_RELEASE}\" = true ]; then git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}; fi")
}       
    shell(
      "generate-env-properties.sh " + REPOSITORY_NAME.toLowerCase() + " 'env.properties' '${COMPILER}'" + ' "${IS_RELEASE}" "${BUILD_NUMBER}"'
		)
    environmentVariables {
      propertiesFile('env.properties')
    }	 
    shell("front-compiler.sh '${REPOSITORY_NAME}' '${DIST_DIR}' '${DIST_INCLUDE}' '${DIST_EXCLUDE}' '${COMPILER}' '${CONFIG_DIRECTORY}'")
  }
  publishers {
    archiveArtifacts('*.zip')
if (JUNIT_TESTS_PATTERN?.trim()) {
    archiveJunit(JUNIT_TESTS_PATTERN)
}
    downstreamParameterized {
      trigger(dockerJobName) {
        condition('SUCCESS')
        parameters {
          propertiesFile('env.properties', true)
          predefinedProp('PIPELINE_VERSION_TEST',GITLAB_PROJECT.toLowerCase()+':${FRONT_IMAGE_VERSION}')
          predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS}")
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
  }
} //job

def updateParam(node, String paramName, String defaultValue) {
  def aux = node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'*'.find {
    it.name != null && it.name.text() == paramName
  }
  aux.defaultValue[0].value = defaultValue
}

// Docker job
job (dockerJobName) {
  println "JOB: "+dockerJobName
  label('front-build-docker')
  deliveryPipelineConfiguration('CI', 'Front Docker Build')
  parameters {
    stringParam('ARTIFACT_NAME', "${REPOSITORY_NAME}", 'Front artifact name')

  }
  wrappers {
    //buildName('${ENV,var="$FRONT_IMAGE_NAME"}:${ENV,var="PIPELINE_VERSION_TEST"}-${BUILD_NUMBER}')
	buildName('${ENV,var="PIPELINE_VERSION_TEST"}')
    credentialsBinding {
      usernamePassword('DOCKER_REGISTRY_USERNAME','DOCKER_REGISTRY_PASSWORD', SERENITY_CREDENTIAL)
    }
  }
  steps {
    copyArtifacts(buildJobName) {
      includePatterns("*.zip")
      flatten()
      optional(false)
      fingerprintArtifacts(false)
      buildSelector {
        latestSuccessful(true)
      }
    }
    shell('generate-and-push-front-image.sh')
  }
  publishers {
    flexiblePublish {
      conditionalAction {
        condition { 
          //if it is a SNAPSHOT deployment is triggered
          expression('(.*)-(\\d+)$', '${ENV,var="FRONT_IMAGE_VERSION"}')
        }
        publishers {
          downstreamParameterized {
            trigger(deployDevJobName) {
              condition('SUCCESS')
              parameters {
                predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS}")
                predefinedProp('PIPELINE_VERSION', '${FRONT_IMAGE_VERSION}')
              }
            }
          }
        }
      }
    }
  }
}
                                
//Deploy in dev job
job (deployDevJobName) {
  println "JOB: " + deployDevJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('DEV', 'Deploy')
  configure {
    updateParam(it, 'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-dev')
    updateParam(it, 'OSE3_APP_NAME', OSE3_APP_NAME)
    updateParam(it, 'OSE3_TEMPLATE_NAME',OSE3_TEMPLATE_NAME)
    updateParam(it, 'OSE3_CREATE_TEMPLATE', 'ON')
    updateParam(it, 'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_DEV)
  }
}

//Deploy in pre job
job (deployPreJobName) {
  println "JOB: " + deployPreJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('PRE', 'Deploy')
  properties {
    promotions {
      promotion {
        name('Promote-Shadow')
        icon('star-gold-e')
        conditions {
          manual('impes-product-owner,impes-technical-lead,impes-developer')
        }
        actions {
          downstreamParameterized {
            trigger(deployHideJobName) {
              parameters {
                predefinedProp('OSE3_TEMPLATE_PARAMS','${OSE3_TEMPLATE_PARAMS}')
                predefinedProp('PIPELINE_VERSION','${PIPELINE_VERSION}')
              }
            }
          }
        }
      }
    }
  }
  configure {
    updateParam(it, 'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
    updateParam(it, 'OSE3_APP_NAME', OSE3_APP_NAME)
    updateParam(it, 'OSE3_TEMPLATE_NAME',OSE3_TEMPLATE_NAME)
    updateParam(it, 'OSE3_CREATE_TEMPLATE', 'ON')
    updateParam(it, 'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_PRE)
  }
}

//Deploy in hide environment jo
job (deployHideJobName) {
  println "JOB: $deployHideJobName"
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('Shadow', 'Deploy to shadow')
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
                predefinedProp('PIPELINE_VERSION', '${PIPELINE_VERSION}')
                predefinedProp('OSE3_TOKEN_PROJECT', '${OSE3_TOKEN_PROJECT}')
                predefinedProp('OSE3_TEMPLATE_PARAMS', '${OSE3_TEMPLATE_PARAMS}')
              }
            }
          }
        }
      }
    }
  }

 
  configure {
    updateParam(it, 'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pro')
    updateParam(it, 'OSE3_APP_NAME', OSE3_APP_NAME)
    updateParam(it, 'OSE3_TEMPLATE_NAME',OSE3_TEMPLATE_NAME)
    updateParam(it, 'OSE3_CREATE_TEMPLATE', 'ON')
    updateParam(it, 'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_PRO)
    updateParam(it, 'OSE3_BLUE_GREEN', 'ON')
  }
}


//Deploy in pro job
job (deployProJobName) {
  println "JOB: $deployProJobName"
  using('TJ-ose3-switch')
  disabled(false)
  deliveryPipelineConfiguration('PRO', 'Switch from Shadow to PRO')
  configure {
    updateParam(it, 'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pro')
    updateParam(it, 'OSE3_APP_NAME', OSE3_APP_NAME)
    updateParam(it, 'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_PRO)
  }
}

gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, buildJobName)
