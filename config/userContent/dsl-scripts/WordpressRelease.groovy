import jenkins.model.*
import java.util.regex.*;
import util.Utilities;

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))
def utils = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/Utils.groovy"))

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def OSE3_URL = "${OSE3_URL}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim().toLowerCase()
def OSE3_APP_NAME="${OSE3_APP_NAME}".trim().toLowerCase()
def GITLAB_CREDENTIAL = "${GITLAB_CREDENTIAL}"
def SERENITY_CREDENTIAL = "${SERENITY_CREDENTIAL}"

// Static values
final String regex = "((?:(?:ssh|git|https?):\\/\\/)?(?:.+(?:(?::.+)?)@)?[\\w\\.]+(?::\\d+)?\\/)?([^\\/\\s]+)\\/([^\\.\\s]+)(?:\\.git)?"
Pattern pattern = Pattern.compile(regex);
Matcher matcher = pattern.matcher(GITLAB_PROJECT);
assert matcher.matches() : "[ERROR] Syntax error: " + GITLAB_PROJECT + " doesn't match expected url pattern."
def GITLAB_SERVER = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger").getGitlabHostUrl();
def GITLAB_API_TOKEN = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger").getGitlabApiToken();
def GITLAB_URL = matcher.group(1) ?: GITLAB_SERVER;
def GROUP_NAME = matcher.group(2);
def REPOSITORY_NAME = matcher.group(3);
out.println("GitLab URL: " + GITLAB_URL);
out.println("GitLab Group: " + GROUP_NAME);
out.println("GitLab Project: " + REPOSITORY_NAME);
GITLAB_PROJECT = GROUP_NAME + '/' + REPOSITORY_NAME
def buildJobName = GITLAB_PROJECT+'-ci-build'
def dockerJobName = GITLAB_PROJECT+'-ci-docker'
def deployDevJobName = GITLAB_PROJECT+'-dev-ose3-deploy'
def deployPreJobName = GITLAB_PROJECT+'-pre-ose3-deploy'
def deployProJobName = GITLAB_PROJECT+'-pro-ose3-deploy'

//DEV
def WORDPRESS_DB_HOST_DEV="${WORDPRESS_DB_HOST_DEV}".trim()
def WORDPRESS_DB_USER_DEV="${WORDPRESS_DB_USER_DEV}".trim()
def WORDPRESS_DB_PASSWORD_DEV="${WORDPRESS_DB_PASSWORD_DEV}".trim()
def WORDPRESS_DB_NAME_DEV="${WORDPRESS_DB_NAME_DEV}".trim()
def S3_BACKUP_HOST_DEV="${S3_BACKUP_HOST_DEV}".trim()
def S3_BACKUP_BUCKET_DEV="${S3_BACKUP_BUCKET_DEV}".trim()
def S3_BACKUP_ACCESS_KEY_DEV="${S3_BACKUP_ACCESS_KEY_DEV}".trim()
def S3_BACKUP_SECRET_KEY_DEV="${S3_BACKUP_SECRET_KEY_DEV}".trim()
def CONFIGURATION_GIT_DEV ="${CONFIGURATION_GIT_DEV}".trim()
def CONTAINER_MEMORY_DEV = "${CONTAINER_MEMORY_DEV}".trim()
def BTSYNC_MEMORY_DEV = "${BTSYNC_MEMORY_DEV}".trim()

//in case the template params, if blank we left the default pf PAAS
def OTHER_OSE3_TEMPLATE_PARAMS_DEV=""
if (WORDPRESS_DB_HOST_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",WORDPRESS_DB_HOST="+WORDPRESS_DB_HOST_DEV
if (WORDPRESS_DB_USER_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",WORDPRESS_DB_USER="+WORDPRESS_DB_USER_DEV
if (WORDPRESS_DB_PASSWORD_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",WORDPRESS_DB_PASSWORD="+WORDPRESS_DB_PASSWORD_DEV
if (WORDPRESS_DB_NAME_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",WORDPRESS_DB_NAME="+WORDPRESS_DB_NAME_DEV
if (S3_BACKUP_HOST_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",S3_BACKUP_HOST="+S3_BACKUP_HOST_DEV
if (S3_BACKUP_BUCKET_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",S3_BACKUP_BUCKET="+S3_BACKUP_BUCKET_DEV
if (S3_BACKUP_ACCESS_KEY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",S3_BACKUP_ACCESS_KEY="+S3_BACKUP_ACCESS_KEY_DEV
if (S3_BACKUP_SECRET_KEY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",S3_BACKUP_SECRET_KEY="+S3_BACKUP_SECRET_KEY_DEV
if (HTTP_PROXY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",http_proxy="+HTTP_PROXY_DEV
if (HTTPS_PROXY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",https_proxy="+HTTPS_PROXY_DEV
if (NO_PROXY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",no_proxy="+NO_PROXY_DEV
if (TZ_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",TZ="+TZ_DEV
if (IGNORELIST_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",IGNORELIST="+IGNORELIST_DEV
if (CONFIGURATION_GIT_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",CONFIGURATION_GIT="+CONFIGURATION_GIT_DEV
if (CONTAINER_MEMORY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",CONTAINER_MEMORY="+CONTAINER_MEMORY_DEV
if (BTSYNC_MEMORY_DEV != "") OTHER_OSE3_TEMPLATE_PARAMS_DEV+=",BTSYNC_MEMORY="+BTSYNC_MEMORY_DEV
def OSE3_TEMPLATE_PARAMS_DEV="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/"+GITLAB_PROJECT+':${PIPELINE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_DEV}"

//PRE
def WORDPRESS_DB_HOST_PRE="${WORDPRESS_DB_HOST_PRE}".trim()
def WORDPRESS_DB_USER_PRE="${WORDPRESS_DB_USER_PRE}".trim()
def WORDPRESS_DB_PASSWORD_PRE="${WORDPRESS_DB_PASSWORD_PRE}".trim()
def WORDPRESS_DB_NAME_PRE="${WORDPRESS_DB_NAME_PRE}".trim()
def S3_BACKUP_HOST_PRE="${S3_BACKUP_HOST_PRE}".trim()
def S3_BACKUP_BUCKET_PRE="${S3_BACKUP_BUCKET_PRE}".trim()
def S3_BACKUP_ACCESS_KEY_PRE="${S3_BACKUP_ACCESS_KEY_PRE}".trim()
def S3_BACKUP_SECRET_KEY_PRE="${S3_BACKUP_SECRET_KEY_PRE}".trim()
def CONFIGURATION_GIT_PRE ="${CONFIGURATION_GIT_PRE}".trim()
def CONTAINER_MEMORY_PRE = "${CONTAINER_MEMORY_PRE}".trim()
def BTSYNC_MEMORY_PRE = "${BTSYNC_MEMORY_PRE}".trim()

//in case the template params, if blank we left the default pf PAAS
def OTHER_OSE3_TEMPLATE_PARAMS_PRE=""
if (WORDPRESS_DB_HOST_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",WORDPRESS_DB_HOST="+WORDPRESS_DB_HOST_PRE
if (WORDPRESS_DB_USER_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",WORDPRESS_DB_USER="+WORDPRESS_DB_USER_PRE
if (WORDPRESS_DB_PASSWORD_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",WORDPRESS_DB_PASSWORD="+WORDPRESS_DB_PASSWORD_PRE
if (WORDPRESS_DB_NAME_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",WORDPRESS_DB_NAME="+WORDPRESS_DB_NAME_PRE
if (S3_BACKUP_HOST_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",S3_BACKUP_HOST="+S3_BACKUP_HOST_PRE
if (S3_BACKUP_BUCKET_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",S3_BACKUP_BUCKET="+S3_BACKUP_BUCKET_PRE
if (S3_BACKUP_ACCESS_KEY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",S3_BACKUP_ACCESS_KEY="+S3_BACKUP_ACCESS_KEY_PRE
if (S3_BACKUP_SECRET_KEY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",S3_BACKUP_SECRET_KEY="+S3_BACKUP_SECRET_KEY_PRE
if (HTTP_PROXY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",http_proxy="+HTTP_PROXY_PRE
if (HTTPS_PROXY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",https_proxy="+HTTPS_PROXY_PRE
if (NO_PROXY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",no_proxy="+NO_PROXY_PRE
if (TZ_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",TZ="+TZ_PRE
if (IGNORELIST_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",IGNORELIST="+IGNORELIST_PRE
if (SECRETBTSYNC_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",SECRETBTSYNC="+SECRETBTSYNC_PRE
if (CONFIGURATION_GIT_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",CONFIGURATION_GIT="+CONFIGURATION_GIT_PRE
if (CONTAINER_MEMORY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",CONTAINER_MEMORY="+CONTAINER_MEMORY_PRE
if (BTSYNC_MEMORY_PRE != "") OTHER_OSE3_TEMPLATE_PARAMS_PRE+=",BTSYNC_MEMORY="+BTSYNC_MEMORY_PRE
def OSE3_TEMPLATE_PARAMS_PRE="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/"+GITLAB_PROJECT+':${PIPELINE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_PRE}"

//PRO
def WORDPRESS_DB_HOST_PRO="${WORDPRESS_DB_HOST_PRO}".trim()
def WORDPRESS_DB_USER_PRO="${WORDPRESS_DB_USER_PRO}".trim()
def WORDPRESS_DB_PASSWORD_PRO="${WORDPRESS_DB_PASSWORD_PRO}".trim()
def WORDPRESS_DB_NAME_PRO="${WORDPRESS_DB_NAME_PRO}".trim()
def S3_BACKUP_HOST_PRO="${S3_BACKUP_HOST_PRO}".trim()
def S3_BACKUP_BUCKET_PRO="${S3_BACKUP_BUCKET_PRO}".trim()
def S3_BACKUP_ACCESS_KEY_PRO="${S3_BACKUP_ACCESS_KEY_PRO}".trim()
def S3_BACKUP_SECRET_KEY_PRO="${S3_BACKUP_SECRET_KEY_PRO}".trim()
def CONFIGURATION_GIT_PRO ="${CONFIGURATION_GIT_PRO}".trim()
def CONTAINER_MEMORY_PRO = "${CONTAINER_MEMORY_PRO}".trim()
def BTSYNC_MEMORY_PRO = "${BTSYNC_MEMORY_PRO}".trim()

//in case the template params, if blank we left the default pf PAAS
def OTHER_OSE3_TEMPLATE_PARAMS_PRO=""
if (WORDPRESS_DB_HOST_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",WORDPRESS_DB_HOST="+WORDPRESS_DB_HOST_PRO
if (WORDPRESS_DB_USER_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",WORDPRESS_DB_USER="+WORDPRESS_DB_USER_PRO
if (WORDPRESS_DB_PASSWORD_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",WORDPRESS_DB_PASSWORD="+WORDPRESS_DB_PASSWORD_PRO
if (WORDPRESS_DB_NAME_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",WORDPRESS_DB_NAME="+WORDPRESS_DB_NAME_PRO
if (S3_BACKUP_HOST_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",S3_BACKUP_HOST="+S3_BACKUP_HOST_PRO
if (S3_BACKUP_BUCKET_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",S3_BACKUP_BUCKET="+S3_BACKUP_BUCKET_PRO
if (S3_BACKUP_ACCESS_KEY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",S3_BACKUP_ACCESS_KEY="+S3_BACKUP_ACCESS_KEY_PRO
if (S3_BACKUP_SECRET_KEY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",S3_BACKUP_SECRET_KEY="+S3_BACKUP_SECRET_KEY_PRO
if (HTTP_PROXY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",http_proxy="+HTTP_PROXY_PRO
if (HTTPS_PROXY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",https_proxy="+HTTPS_PROXY_PRO
if (NO_PROXY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",no_proxy="+NO_PROXY_PRO
if (TZ_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",TZ="+TZ_PRO
if (IGNORELIST_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",IGNORELIST="+IGNORELIST_PRO
if (SECRETBTSYNC_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",SECRETBTSYNC="+SECRETBTSYNC_PRO
if (CONFIGURATION_GIT_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",CONFIGURATION_GIT="+CONFIGURATION_GIT_PRO
if (CONTAINER_MEMORY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",CONTAINER_MEMORY="+CONTAINER_MEMORY_PRO
if (BTSYNC_MEMORY_PRO != "") OTHER_OSE3_TEMPLATE_PARAMS_PRO+=",BTSYNC_MEMORY="+BTSYNC_MEMORY_PRO

def OSE3_TEMPLATE_PARAMS_PRO="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/"+GITLAB_PROJECT+':${PIPELINE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_PRO}"

//creck gitlab credentials
def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL)
if ( gitlabCredsType == null ) {
  throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
}
println ("GitLab credential type " + gitlabCredsType );

// Build job
job (buildJobName) {
  println "JOB: "+buildJobName
  label('wordpress-build')
  deliveryPipelineConfiguration('CI', 'Build&Package')
  logRotator(daysToKeep=30, numToKeep=10, artifactDaysToKeep=-1,artifactNumToKeep=-1)

  parameters {
    // Defines a simple text parameter, where users can enter a string value.
    stringParam('gitlabActionType', 'PUSH', 'GitLab Event (PUSH or MERGE)')
    stringParam('gitlabSourceRepoURL', GITLAB_SERVER+'/'+GITLAB_PROJECT+'.git', 'GitLab Source Repository')
    stringParam('gitlabSourceRepoName', 'origin', 'GitLab source repo name (only for MERGE events from forked repositories)')
    stringParam('gitlabSourceBranch', GIT_INTEGRATION_BRANCH, 'Gitlab source branch (only for MERGE events from forked repositories)')
    stringParam('gitlabTargetBranch', GIT_INTEGRATION_BRANCH, 'GitLab target branch (only for MERGE events)')
  }

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
                predefinedProp('PIPELINE_VERSION','${WORDPRESS_IMAGE_VERSION}')
              }
            }
          }
        }
      }
      promotion {
        name('DEV')
        icon('star-gold-e')
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
      branch('${gitlabSourceRepoName}/${gitlabSourceBranch}')
      browser {
        gitLab(GITLAB_URL+GITLAB_PROJECT, '8.2')
      } //browser
      remote {
        credentials(GITLAB_CREDENTIAL)
        name('origin')
        url(GITLAB_SERVER+GITLAB_PROJECT+'.git')
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
      includeBranches(GIT_INTEGRATION_BRANCH)
    }
  } //triggers

  wrappers {
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

    buildName(OSE3_APP_NAME+'-${ENV,var="WORDPRESS_IMAGE_VERSION"}-${BUILD_NUMBER}')
    release {
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","WORDPRESS_IMAGE_VERSION"]')
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
    shell('parse_yaml.sh application.yml > env.properties')
    environmentVariables {
      propertiesFile('env.properties')
    }
    shell('zip -r wordpress.zip application.yml wp-content/')
  }// steps

  publishers {
    archiveArtifacts('**/*.zip')
    downstreamParameterized {
      trigger(dockerJobName) {
        condition('SUCCESS')
        parameters {
          propertiesFile('env.properties', true)
          predefinedProp('PIPELINE_VERSION_TEST',GITLAB_PROJECT+':${WORDPRESS_IMAGE_VERSION}')
        }
      }
    }
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

// Docker job
job (dockerJobName) {
  println "JOB: "+dockerJobName
  label('wordpress-docker')
  deliveryPipelineConfiguration('CI', 'Docker Build')
  parameters {
    stringParam('ARTIFACT_NAME', 'wordpress.zip', 'Wordpress artifact name')
  }

  wrappers {
    buildName('${ENV,var="PIPELINE_VERSION_TEST"}-${BUILD_NUMBER}')
    credentialsBinding {
      usernamePassword('DOCKER_REGISTRY_USERNAME','DOCKER_REGISTRY_PASSWORD', SERENITY_CREDENTIAL)
      usernamePassword('OSE3_USERNAME','OSE3_PASSWORD', SERENITY_CREDENTIAL)
    }
  }
  steps {
    copyArtifacts(buildJobName) {
      includePatterns('**/*.zip')
      flatten()
      optional(false)
      fingerprintArtifacts(false)
      buildSelector {
        latestSuccessful(true)
      }
    }
    shell('generate-and-push-wordpress-image.sh')
  }

  publishers {
    downstreamParameterized {
      trigger(deployDevJobName) {
        condition('SUCCESS')
        parameters {
          predefinedProp('OSE3_USERNAME','${OSE3_USERNAME}')
          predefinedProp('OSE3_PASSWORD','${OSE3_PASSWORD}')
          predefinedProp('PIPELINE_VERSION','${WORDPRESS_IMAGE_VERSION}')
        }
      }
    }
  }
}

def updateParam(node, String paramName, String defaultValue) {
  def aux = node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'*'.find {
    it.name != null && it.name.text() == paramName
  }
  aux.defaultValue[0].value = defaultValue
}

//Deploy in dev job
job (deployDevJobName) {
  println "JOB: " + deployDevJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('DEV', 'Deploy')
  configure {
    updateParam(it,'OSE3_URL', OSE3_URL)
    updateParam(it,'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-dev')
    updateParam(it,'OSE3_APP_NAME',OSE3_APP_NAME) 
    updateParam(it,'OSE3_TEMPLATE_NAME',OSE3_TEMPLATE_NAME) 
    updateParam(it,'OSE3_TEMPLATE_PARAMS',OSE3_TEMPLATE_PARAMS_DEV) 
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
        name('Promote-PRO')
        icon('star-gold-e')
        conditions {
          manual('impes-product-owner,impes-technical-lead,impes-developer')
        }
        actions {
          downstreamParameterized {
            trigger(deployProJobName) {
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
    updateParam(it,'OSE3_URL', OSE3_URL)
    updateParam(it,'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
    updateParam(it,'OSE3_APP_NAME',OSE3_APP_NAME) 
    updateParam(it,'OSE3_TEMPLATE_NAME',OSE3_TEMPLATE_NAME) 
    updateParam(it,'OSE3_TEMPLATE_PARAMS',OSE3_TEMPLATE_PARAMS_PRE) 
  }
}

//Deploy in pro job
job (deployProJobName) {
  println "JOB: " + deployProJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('PRO', 'Deploy')
  configure {
    updateParam(it,'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pro')
    updateParam(it,'OSE3_APP_NAME',OSE3_APP_NAME) 
    updateParam(it,'OSE3_TEMPLATE_NAME',OSE3_TEMPLATE_NAME)
    updateParam(it,'OSE3_TEMPLATE_PARAMS',OSE3_TEMPLATE_PARAMS_PRO)
  }
}

gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, buildJobName)
