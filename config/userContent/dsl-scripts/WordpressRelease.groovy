import jenkins.model.*

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim()
def OSE3_URL = "${OSE3_URL}".trim()
def SERENITY_CREDENTIAL = "${SERENITY_CREDENTIAL}"

def OSE3_APP_NAME="${OSE3_APP_NAME}".trim()
def WORDPRESS_DOCKER_REPOSITORY_NAME="${WORDPRESS_DOCKER_REPOSITORY_NAME}".trim()
//DEV
def WORDPRESS_DB_HOST_DEV="${WORDPRESS_DB_HOST_DEV}".trim()
def WORDPRESS_DB_USER_DEV="${WORDPRESS_DB_USER_DEV}".trim()
def WORDPRESS_DB_PASSWORD_DEV="${WORDPRESS_DB_PASSWORD_DEV}".trim()
def WORDPRESS_DB_NAME_DEV="${WORDPRESS_DB_NAME_DEV}".trim()
def S3_BACKUP_HOST_DEV="${S3_BACKUP_HOST_DEV}".trim()
def S3_BACKUP_BUCKET_DEV="${S3_BACKUP_BUCKET_DEV}".trim()
def S3_BACKUP_ACCESS_KEY_DEV="${S3_BACKUP_ACCESS_KEY_DEV}".trim()
def S3_BACKUP_SECRET_KEY_DEV="${S3_BACKUP_SECRET_KEY_DEV}".trim()

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
def OSE3_TEMPLATE_PARAMS_DEV="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/${WORDPRESS_DOCKER_REPOSITORY_NAME}:"+'${WORDPRESS_IMAGE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_DEV}"

//PRE
def WORDPRESS_DB_HOST_PRE="${WORDPRESS_DB_HOST_PRE}".trim()
def WORDPRESS_DB_USER_PRE="${WORDPRESS_DB_USER_PRE}".trim()
def WORDPRESS_DB_PASSWORD_PRE="${WORDPRESS_DB_PASSWORD_PRE}".trim()
def WORDPRESS_DB_NAME_PRE="${WORDPRESS_DB_NAME_PRE}".trim()
def S3_BACKUP_HOST_PRE="${S3_BACKUP_HOST_PRE}".trim()
def S3_BACKUP_BUCKET_PRE="${S3_BACKUP_BUCKET_PRE}".trim()
def S3_BACKUP_ACCESS_KEY_PRE="${S3_BACKUP_ACCESS_KEY_PRE}".trim()
def S3_BACKUP_SECRET_KEY_PRE="${S3_BACKUP_SECRET_KEY_PRE}".trim()

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
def OSE3_TEMPLATE_PARAMS_PRE="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/${WORDPRESS_DOCKER_REPOSITORY_NAME}:"+'${WORDPRESS_IMAGE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_PRE}"

//PRO
def WORDPRESS_DB_HOST_PRO="${WORDPRESS_DB_HOST_PRO}".trim()
def WORDPRESS_DB_USER_PRO="${WORDPRESS_DB_USER_PRO}".trim()
def WORDPRESS_DB_PASSWORD_PRO="${WORDPRESS_DB_PASSWORD_PRO}".trim()
def WORDPRESS_DB_NAME_PRO="${WORDPRESS_DB_NAME_PRO}".trim()
def S3_BACKUP_HOST_PRO="${S3_BACKUP_HOST_PRO}".trim()
def S3_BACKUP_BUCKET_PRO="${S3_BACKUP_BUCKET_PRO}".trim()
def S3_BACKUP_ACCESS_KEY_PRO="${S3_BACKUP_ACCESS_KEY_PRO}".trim()
def S3_BACKUP_SECRET_KEY_PRO="${S3_BACKUP_SECRET_KEY_PRO}".trim()

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
def OSE3_TEMPLATE_PARAMS_PRO="APP_NAME=${OSE3_APP_NAME},DOCKER_IMAGE=registry.lvtc.gsnet.corp/${WORDPRESS_DOCKER_REPOSITORY_NAME}:"+'${WORDPRESS_IMAGE_VERSION}'+"${OTHER_OSE3_TEMPLATE_PARAMS_PRO}"

// Static values
def gitlab = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
def GITLAB_SERVER = gitlab.getGitlabHostUrl()
def (GROUP_NAME, REPOSITORY_NAME) = GITLAB_PROJECT.tokenize('/')
def buildJobName = GITLAB_PROJECT+'-ci-build'
def dockerJobName = GITLAB_PROJECT+'-ci-docker'
def deployDevJobName = GITLAB_PROJECT+'-dev-ose3-deploy'
def deployPreJobName = GITLAB_PROJECT+'-pre-ose3-deploy'
def deployProJobName = GITLAB_PROJECT+'-pro-ose3-deploy'

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
          manual('') {
          }
        }
        actions {
          downstreamParameterized {
            trigger(deployPreJobName,'SUCCESS') {
              parameters {
                predefinedProp('OSE3_CREDENTIAL', SERENITY_CREDENTIAL)
                predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS_PRE}")
                predefinedProp('WORDPRESS_IMAGE_VERSION','${WORDPRESS_IMAGE_VERSION}')
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
    buildName(OSE3_APP_NAME+'-${ENV,var="WORDPRESS_IMAGE_VERSION"}-${BUILD_NUMBER}')
    release {
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["WORDPRESS_IMAGE_VERSION"]')
        }
      }
      // Adds build steps to run before the release.
      preBuildSteps {
        shell("git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}")
      }
      configure {
        it / delegate.postSuccessfulBuildSteps {
          'hudson.plugins.git.GitPublisher'(plugin: 'git@2.4.1') {
            configVersion(2)
            pushMerge(false)
            pushOnlyIfSuccess(false)
            forcePush(false)
            tagsToPush {
              'hudson.plugins.git.GitPublisher_-TagToPush' {
                targetRepoName('origin')
                tagName('v${WORDPRESS_IMAGE_VERSION}')
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
          'hudson.tasks.Shell' {
            command("git checkout ${GIT_INTEGRATION_BRANCH}")
          }
          'hudson.plugins.git.GitPublisher'(plugin: 'git@2.4.1') {
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
        }
      }
    } //release
  } //wrappers

  steps {
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
          predefinedProp('DOCKER_REGISTRY_CREDENTIAL',SERENITY_CREDENTIAL)
          predefinedProp('OSE3_TEMPLATE_PARAMS_DEV',"${OSE3_TEMPLATE_PARAMS_DEV}")
        }
      }
    }
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
} //job

// Docker job
job (dockerJobName) {
  println "JOB: "+dockerJobName
  label('wordpress-docker')
  deliveryPipelineConfiguration('CI', 'Docker Build')
  parameters {
    stringParam('ARTIFACT_NAME', 'wordpress.zip', 'Wordpress artifact name')
    credentialsParam('DOCKER_REGISTRY_CREDENTIAL') {
      type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
      required(false)
      defaultValue(SERENITY_CREDENTIAL)
      description('Docker Registry credential')
    }
  }

  wrappers {
    buildName('${ENV,var="PIPELINE_VERSION_TEST"}-${BUILD_NUMBER}')
    credentialsBinding {
      usernamePassword('DOCKER_REGISTRY_USERNAME','DOCKER_REGISTRY_PASSWORD', '${DOCKER_REGISTRY_CREDENTIAL}')
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
          predefinedProp('OSE3_CREDENTIAL', SERENITY_CREDENTIAL)
          predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS_DEV}")
          predefinedProp('WORDPRESS_IMAGE_VERSION', '${WORDPRESS_IMAGE_VERSION}')
        }
      }
    }
  }
}

//Deploy in dev job
job (deployDevJobName) {
  println "JOB: " + deployDevJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('DEV', 'Deploy')
  parameters {
    stringParam('OSE3_APP_NAME', "${OSE3_APP_NAME}", 'OSE3 application name')
    stringParam('OSE3_PROJECT_NAME', "${OSE3_PROJECT_NAME}-dev", 'OSE3 project name')
    stringParam('OSE3_URL', "${OSE3_URL}", 'OSE3 URL')
    stringParam('OSE3_TEMPLATE_NAME', "${OSE3_TEMPLATE_NAME}", 'OSE3 template name')
    stringParam('OSE3_TEMPLATE_PARAMS' , '', 'OSE3 template params')
    credentialsParam('OSE3_CREDENTIAL') {
      type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
      required(false)
      defaultValue(SERENITY_CREDENTIAL)
      description('OSE3 credentials')
    }
    stringParam('WORDPRESS_IMAGE_VERSION' , '', 'Pipeline version')
  }
  wrappers {
    buildName('${ENV,var="OSE3_APP_NAME"}:${ENV,var="WORDPRESS_IMAGE_VERSION"}-${BUILD_NUMBER}')
    credentialsBinding {
      usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', '${OSE3_CREDENTIAL}')
    }
  }
  steps {
    shell('deploy_in_ose3.sh')
  }
}

//Deploy in pre job
job (deployPreJobName) {
  println "JOB: " + deployPreJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('PRE', 'Deploy')
  parameters {
    stringParam('OSE3_APP_NAME', "${OSE3_APP_NAME}", 'OSE3 application name')
    stringParam('OSE3_PROJECT_NAME', "${OSE3_PROJECT_NAME}-pre", 'OSE3 project name')
    stringParam('OSE3_URL', "${OSE3_URL}", 'OSE3 URL')
    stringParam('OSE3_TEMPLATE_NAME', "${OSE3_TEMPLATE_NAME}", 'OSE3 template name')
    stringParam('OSE3_TEMPLATE_PARAMS' , '', 'OSE3 template params')
    credentialsParam('OSE3_CREDENTIAL') {
      type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
      required(false)
      defaultValue(SERENITY_CREDENTIAL)
      description('OSE3 credentials')
    }
    stringParam('WORDPRESS_IMAGE_VERSION' , '', 'Pipeline version')
  }
  wrappers {
    buildName('${ENV,var="OSE3_APP_NAME"}:${ENV,var="WORDPRESS_IMAGE_VERSION"}-${BUILD_NUMBER}')
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
          manual('impes-product-owner,impes-technical-lead,impes-developer') {}
        }
        actions {
          downstreamParameterized {
            trigger(deployProJobName, 'SUCCESS') {
              parameters {
                predefinedProp('OSE3_CREDENTIAL', '${OSE3_CREDENTIAL}')
                predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS_PRO}")
                predefinedProp('WORDPRESS_IMAGE_VERSION','${WORDPRESS_IMAGE_VERSION}')
              }
            }
          }
        }
      }
    }
  }
  steps {
    shell('deploy_in_ose3.sh')
  }
}

//Deploy in pro job
job (deployProJobName) {
  println "JOB: " + deployProJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('PRO', 'Deploy')
  parameters {
    stringParam('OSE3_APP_NAME', "${OSE3_APP_NAME}", 'OSE3 application name')
    stringParam('OSE3_PROJECT_NAME', "${OSE3_PROJECT_NAME}-pro", 'OSE3 project name')
    stringParam('OSE3_URL', "${OSE3_URL}", 'OSE3 URL')
    stringParam('OSE3_TEMPLATE_NAME', "${OSE3_TEMPLATE_NAME}", 'OSE3 template name')
    stringParam('OSE3_TEMPLATE_PARAMS' , '', 'OSE3 template params')
    credentialsParam('OSE3_CREDENTIAL') {
      type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
      required(false)
      defaultValue(SERENITY_CREDENTIAL)
      description('OSE3 credentials')
    }
    stringParam('WORDPRESS_IMAGE_VERSION' , '', 'Pipeline version')
  }
  wrappers {
    buildName('${ENV,var="OSE3_APP_NAME"}:${ENV,var="WORDPRESS_IMAGE_VERSION"}-${BUILD_NUMBER}')
    credentialsBinding {
      usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', '${OSE3_CREDENTIAL}')
    }
  }
  steps {
    shell('deploy_in_ose3.sh')
  }
}
