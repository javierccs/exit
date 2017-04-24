import jenkins.model.*
import java.util.regex.*;
import util.Utilities;
import util.AuthorizationJobFactory
import util.OSE3DeployJobFactory

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def OSE3_URL = "${OSE3_URL}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim().toLowerCase()
def OSE3_APP_NAME="${OSE3_APP_NAME}".trim().toLowerCase()
def GITLAB_CREDENTIAL = "${GITLAB_CREDENTIAL}"

// if true generates blue green deployment jobs
boolean blueGreenDeployment = OSE3_BLUE_GREEN_DEPLOYMENT.toBoolean()
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
def dockerJobName = GITLAB_PROJECT+'-ci-build'
def deployDevJobName = GITLAB_PROJECT+'-dev-ose3-deploy'
def deployPreCheckJobName = GITLAB_PROJECT+'-ose3-pre-check-deploy'
def deployProCheckJobName = GITLAB_PROJECT+'-ose3-pro-check-deploy'
def deployPreJobName = GITLAB_PROJECT+'-pre-ose3-deploy'
def deployHideJobName = OSE3DeployJobFactory.getHideJobName(GITLAB_PROJECT)
def deployProJobName = OSE3DeployJobFactory.getProJobName(blueGreenDeployment, GITLAB_PROJECT)

//DEV
def OSE3_TOKEN_PROJECT_DEV="${OSE3_TOKEN_PROJECT_DEV}".trim()
def JAVA_OPTS_EXT_DEV="${JAVA_OPTS_EXT_DEV}".trim()
def WILY_MOM_FQDN_DEV="${WILY_MOM_FQDN_DEV}".trim()
def WILY_MOM_PORT_DEV="${WILY_MOM_PORT_DEV}".trim()
def TZ_DEV="${TZ_DEV}".trim()
def CONFIGURATION_GIT_DEV ="${CONFIGURATION_GIT_DEV}".trim()
def CONFIGURATION_GIT_USR_DEV ="${CONFIGURATION_GIT_USR_DEV}".trim()
def CONFIGURATION_GIT_PASS_DEV ="${CONFIGURATION_GIT_PASS_DEV}".trim()
def CONTAINER_MEMORY_DEV = "${CONTAINER_MEMORY_DEV}".trim()
def VOLUME_CAPACITY_DEV = "${VOLUME_CAPACITY_DEV}".trim()

//in case the template params, if blank we left the default pf PAAS
def OTHER_OSE3_TEMPLATE_PARAMS_DEV=""
if (JAVA_OPTS_EXT_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",JAVA_OPTS_EXT="+JAVA_OPTS_EXT_DEV
if (WILY_MOM_PORT_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",WILY_MOM_PORT="+WILY_MOM_PORT_DEV
if (WILY_MOM_FQDN_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",WILY_MOM_FQDN="+WILY_MOM_FQDN_DEV
if (TZ_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",TZ="+TZ_DEV
if (CONFIGURATION_GIT_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",CONFIGURATION_GIT="+CONFIGURATION_GIT_DEV
if (CONFIGURATION_GIT_USR_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",CONFIGURATION_GIT_USR="+CONFIGURATION_GIT_USR_DEV
if (CONFIGURATION_GIT_PASS_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",CONFIGURATION_GIT_PASS="+CONFIGURATION_GIT_PASS_DEV
if (CONTAINER_MEMORY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",CONTAINER_MEMORY="+CONTAINER_MEMORY_DEV
if (VOLUME_CAPACITY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",VOLUME_CAPACITY="+VOLUME_CAPACITY_DEV
def OSE3_TEMPLATE_PARAMS_DEV="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/"+GITLAB_PROJECT+':${PIPELINE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_DEV}"

//PRE
def OSE3_TOKEN_PROJECT_PRE=""
def JAVA_OPTS_EXT_PRE="${JAVA_OPTS_EXT_PRE}".trim()
def WILY_MOM_PORT_PRE="${WILY_MOM_PORT_PRE}".trim()
def WILY_MOM_FQDN_PRE="${WILY_MOM_FQDN_PRE}".trim()
def TZ_PRE="${TZ_PRE}".trim()
def CONFIGURATION_GIT_PRE ="${CONFIGURATION_GIT_PRE}".trim()
def CONFIGURATION_GIT_USR_PRE ="${CONFIGURATION_GIT_USR_PRE}".trim()
def CONFIGURATION_GIT_PASS_PRE ="${CONFIGURATION_GIT_PASS_PRE}".trim()
def CONTAINER_MEMORY_PRE = "${CONTAINER_MEMORY_PRE}".trim()
def VOLUME_CAPACITY_PRE = "${VOLUME_CAPACITY_PRE}".trim()

//in case the template params, if blank we left the default pf PAAS
def OTHER_OSE3_TEMPLATE_PARAMS_PRE=""
if (JAVA_OPTS_EXT_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",JAVA_OPTS_EXT="+JAVA_OPTS_EXT_PRE
if (WILY_MOM_PORT_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",WILY_MOM_PORT="+WILY_MOM_PORT_PRE
if (WILY_MOM_FQDN_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",WILY_MOM_FQDN="+WILY_MOM_FQDN_PRE
if (TZ_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",TZ="+TZ_PRE
if (CONFIGURATION_GIT_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",CONFIGURATION_GIT="+CONFIGURATION_GIT_PRE
if (CONFIGURATION_GIT_USR_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",CONFIGURATION_GIT_USR="+CONFIGURATION_GIT_USR_PRE
if (CONFIGURATION_GIT_PASS_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",CONFIGURATION_GIT_PASS="+CONFIGURATION_GIT_PASS_PRE
if (CONTAINER_MEMORY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",CONTAINER_MEMORY="+CONTAINER_MEMORY_PRE
if (VOLUME_CAPACITY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",VOLUME_CAPACITY="+VOLUME_CAPACITY_PRE
def OSE3_TEMPLATE_PARAMS_PRE="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/"+GITLAB_PROJECT+':${PIPELINE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_PRE}"

//PRO
def OSE3_TOKEN_PROJECT_PRO=""
def JAVA_OPTS_EXT_PRO="${JAVA_OPTS_EXT_PRO}".trim()
def WILY_MOM_PORT_PRO="${WILY_MOM_PORT_PRO}".trim()
def WILY_MOM_FQDN_PRO="${WILY_MOM_FQDN_PRO}".trim()
def TZ_PRO="${TZ_PRO}".trim()
def CONFIGURATION_GIT_PRO ="${CONFIGURATION_GIT_PRO}".trim()
def CONFIGURATION_GIT_USR_PRO ="${CONFIGURATION_GIT_USR_PRO}".trim()
def CONFIGURATION_GIT_PASS_PRO ="${CONFIGURATION_GIT_PASS_PRO}".trim()
def CONTAINER_MEMORY_PRO = "${CONTAINER_MEMORY_PRO}".trim()
def VOLUME_CAPACITY_PRO = "${VOLUME_CAPACITY_PRO}".trim()

//in case the template params, if blank we left the default pf PAAS
def OTHER_OSE3_TEMPLATE_PARAMS_PRO=""
if (JAVA_OPTS_EXT_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",JAVA_OPTS_EXT="+JAVA_OPTS_EXT_PRO
if (WILY_MOM_PORT_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",WILY_MOM_PORT="+WILY_MOM_PORT_PRO
if (WILY_MOM_FQDN_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",WILY_MOM_FQDN="+WILY_MOM_FQDN_PRO
if (TZ_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",TZ="+TZ_PRO
if (CONFIGURATION_GIT_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",CONFIGURATION_GIT="+CONFIGURATION_GIT_PRO
if (CONFIGURATION_GIT_USR_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",CONFIGURATION_GIT_USR="+CONFIGURATION_GIT_USR_PRO
if (CONFIGURATION_GIT_PASS_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",CONFIGURATION_GIT_PASS="+CONFIGURATION_GIT_PASS_PRO
if (CONTAINER_MEMORY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",CONTAINER_MEMORY="+CONTAINER_MEMORY_PRO
if (VOLUME_CAPACITY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",VOLUME_CAPACITY="+VOLUME_CAPACITY_PRO
def OSE3_TEMPLATE_PARAMS_PRO="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/"+GITLAB_PROJECT+':${PIPELINE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_PRO}"

//creck gitlab credentials
def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL, GITLAB_URL)
if ( gitlabCredsType == null ) {
  throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
}
out.println ("GitLab credential type " + gitlabCredsType );

def removeParam(node, String paramName) {
  def aux = node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'*'.find {
    it.name.text() == paramName
  }
  node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions[0].remove(aux)
}

// Docker job
job (dockerJobName) {
  out.println "JOB: "+dockerJobName
  label('liferay-docker')
  deliveryPipelineConfiguration('CI', 'Docker Build')
  logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)

  parameters {
    // Defines a simple text parameter, where users can enter a string value.
    stringParam('gitlabActionType', 'PUSH', 'GitLab Event (PUSH or MERGE)')
    stringParam('gitlabSourceRepoURL', GITLAB_URL+GITLAB_PROJECT+'.git', 'GitLab Source Repository')
    stringParam('gitlabSourceRepoName', 'origin', 'GitLab source repo name (only for MERGE events from forked repositories)')
    stringParam('gitlabSourceBranch', GIT_INTEGRATION_BRANCH, 'Gitlab source branch (only for MERGE events from forked repositories)')
    stringParam('gitlabTargetBranch', GIT_INTEGRATION_BRANCH, 'GitLab target branch (only for MERGE events)')
  }

  properties{
    promotions{
      promotion {
        name('DEV')
           icon('star-blue')
           conditions {
             downstream(false, deployDevJobName)
           }
      }
      promotion {
        name('PRE-Check')
        icon('star-purple')
        conditions {
          releaseBuild()
          selfPromotion(false)
        }
        actions {
          downstreamParameterized {
            trigger(deployPreCheckJobName) {
              parameters {
                predefinedProp('PIPELINE_VERSION','${LIFERAY_IMAGE_VERSION}')
              }
            }
          }
        }
      }
      promotion {
        name('PRE')
        icon('star-silver-w')
        conditions {
          downstream(false, deployPreJobName)
        }
      }
if (blueGreenDeployment) {
      promotion {
        name('Shadow')
        icon('star-gold-w')
        conditions {
          downstream(false, deployHideJobName)
        }
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
        gitLab(GITLAB_SERVER+GITLAB_PROJECT, '8.2')
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
    credentialsBinding {
      usernamePassword('DOCKER_REGISTRY_USERNAME','DOCKER_REGISTRY_PASSWORD', 'docker-registry-credential-id')
//If user password credentials are provided bind is required
if ( gitlabCredsType == 'UserPassword' ){
      usernamePassword('GITLAB_CREDENTIAL', GITLAB_CREDENTIAL)
}
    }
//if ssh credentials ssAgent is added
if ( gitlabCredsType == 'SSH' ){
    sshAgent(GITLAB_CREDENTIAL)
}

    buildName(OSE3_APP_NAME+'-${ENV,var="LIFERAY_IMAGE_VERSION"}-${BUILD_NUMBER}')
    release {
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","LIFERAY_IMAGE_VERSION"]')
        }
      }
      // Adds build steps to run before the release.
      preBuildSteps {
        environmentVariables {
          env('IS_RELEASE',true)
        }
      }
      postSuccessfulBuildSteps {
        shell("git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}")
      }
    } //release
  } //wrappers

  steps {
    shell("if [ \"\${IS_RELEASE}\" = true ]; then git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}; fi")
    shell('parse_yaml.sh application.yml > env.properties\n'+
          'parse-dependencies-xml.sh dependencies.xml')
    environmentVariables {
      propertiesFile('env.properties')
    }
    //TODO: get all files from yml
    shell("export PIPELINE_VERSION=$GITLAB_PROJECT"+':${LIFERAY_IMAGE_VERSION//SNAPSHOT/SNAPSHOT-$BUILD_NUMBER}\n'+
          'generate-and-push-liferay-image.sh')
  }// steps

  publishers {
    flexiblePublish {
      conditionalAction {
        condition { not { booleanCondition('${ENV,var="IS_RELEASE"}') } }
        publishers {
          downstreamParameterized {
            trigger(deployDevJobName) {
              condition('SUCCESS')
              parameters {
                predefinedProp('PIPELINE_VERSION','${LIFERAY_IMAGE_VERSION}-${BUILD_NUMBER}')
              } //parameters
            }//trigger
          } //downstream
        } //publishers
      } //conditionalAction
    }//flexiblePublish
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
} //job

def updateParam(node, String paramName, String defaultValue) {
  def aux = node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'*'.find {
    it.name != null && it.name.text() == paramName
  }
  aux.defaultValue[0].value = defaultValue
}

//Deploy in dev job
job (deployDevJobName) {
  out.println "JOB: " + deployDevJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('DEV', 'Deploy')
  configure {
    removeParam(it, 'CERTIFICATE')
    removeParam(it, 'PRIVATE_KEY_CERTIFICATE')
    removeParam(it, 'CA_CERTIFICATE')
    updateParam(it,'OSE3_URL', OSE3_URL)
    updateParam(it,'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-dev')
    updateParam(it,'OSE3_APP_NAME',OSE3_APP_NAME)
    updateParam(it,'OSE3_TEMPLATE_NAME',OSE3_TEMPLATE_NAME)
    updateParam(it,'OSE3_TEMPLATE_PARAMS',OSE3_TEMPLATE_PARAMS_DEV)
    updateParam(it,'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_DEV)
  }
}


def approvalJobArgs = [
  ['PIPELINE_VERSION','${PIPELINE_VERSION}']

]
def approvalJobBuildName = '${ENV,var="PIPELINE_VERSION_TEST"}-${BUILD_NUMBER}'
// pre approval job
AuthorizationJobFactory.createApprovalJob(this,
  deployPreCheckJobName, false, approvalJobBuildName,
  approvalJobArgs, deployPreJobName)

//Deploy in pre job
job (deployPreJobName) {
  out.println "JOB: " + deployPreJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('PRE', 'Deploy')
  properties {
    promotions {
      promotion {
        name('PRO-Check')
        icon('star-purple')
        conditions {
          manual(Utilities.getPrePromotionRoleGroups())
        }
        actions {
          downstreamParameterized {
            trigger(deployProCheckJobName) {
              parameters {
                predefinedProp('PIPELINE_VERSION','${PIPELINE_VERSION}')
              }
            }
          }
        }
      }
    }
  }
 configure {
    removeParam(it, 'CERTIFICATE')
    removeParam(it, 'PRIVATE_KEY_CERTIFICATE')
    removeParam(it, 'CA_CERTIFICATE')
    updateParam(it,'OSE3_URL', OSE3_URL)
    updateParam(it,'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
    updateParam(it,'OSE3_APP_NAME',OSE3_APP_NAME)
    updateParam(it,'OSE3_TEMPLATE_NAME',OSE3_TEMPLATE_NAME)
    updateParam(it,'OSE3_TEMPLATE_PARAMS',OSE3_TEMPLATE_PARAMS_PRE)
    updateParam(it,'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_PRE)

  }
}

// production approval job
AuthorizationJobFactory.createApprovalJob(this,
  deployProCheckJobName, true, approvalJobBuildName,
  approvalJobArgs, deployHideJobName)

// pro deployment Jobs
OSE3DeployJobFactory.createOse3ProJobs (this, blueGreenDeployment,
  approvalJobBuildName,
  approvalJobArgs, GITLAB_PROJECT, OSE3_URL, OSE3_PROJECT_NAME + "-pro",
  OSE3_APP_NAME, OSE3_TEMPLATE_NAME, OSE3_TEMPLATE_PARAMS_PRO)

gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, dockerJobName)
