import jenkins.model.*

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim()
def SERENITY_CREDENTIAL = "${SERENITY_CREDENTIAL}"

// Static values
def gitlab = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
def GITLAB_SERVER = gitlab.getGitlabHostUrl()
def REPOSITORY_NAME = GITLAB_PROJECT.substring(GITLAB_PROJECT.indexOf('/')+1)
def buildJobName = GITLAB_PROJECT+'-release-build'
def dockerJobName = GITLAB_PROJECT+'-release-docker'

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
    stringParam('gitlabSourceBranch', GIT_RELEASE_BRANCH, 'Gitlab source branch (only for MERGE events from forked repositories)')
    stringParam('gitlabTargetBranch', GIT_RELEASE_BRANCH, 'GitLab target branch (only for MERGE events)')
  }

  properties{
    promotions{
      promotion {
        name('PreProduction')
        icon('star-gold-w')
        conditions {
          downstream(false, 'wp-pre-ose3-deploy')
        }
      }
      promotion {
        name('Production')
        icon('star-gold')
        conditions {
          downstream(false, 'wp-pro-ose3-deploy')
        }
      }
    }
  }

  wrappers {
    deliveryPipelineVersion(GITLAB_PROJECT+':1.${BUILD_NUMBER}', true)
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
      mergeOptions('origin', '${gitlabTargetBranch}')
    } //git
  } //scm

  triggers {
    gitlabPush {
      buildOnPushEvents(true)
      buildOnMergeRequestEvents(true)
      setBuildDescription(true)
      useCiFeatures(false)
      allowAllBranches(false)
      includeBranches(GIT_RELEASE_BRANCH)
    }
  } //triggers

  steps {
    shell('zip -r wordpress.zip docker-compose.yml wp-content/')
  }// steps
  publishers {
    archiveArtifacts('**/*.zip')
    git {
      pushOnlyIfSuccess()
      tag('origin', '1.$BUILD_NUMBER') {
        message('DOCKER IMAGE TAG')
        create()
      }
    }
    downstreamParameterized {
      trigger(dockerJobName) {
        condition('SUCCESS')
        parameters {
          predefinedProp('PIPELINE_VERSION','${PIPELINE_VERSION}')
        }
      }
    }
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

  properties {
    promotions{
      promotion {
        name('Promote-pre')
        icon('star-gold-w')
        conditions {
          manual('') {
          }
        }
        actions {
          downstreamParameterized {
            trigger('wp-pre-ose3-deploy','SUCCESS') {
              //condition('SUCCESS')
              parameters {
                predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME)
                predefinedProp('OSE3_CREDENTIAL', SERENITY_CREDENTIAL)
                predefinedProp('OSE3_APP_NAME', REPOSITORY_NAME)
                predefinedProp('OSE3_TEMPLATE_NAME',"${OSE3_TEMPLATE_NAME}".trim())
                predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS}".trim())
              }
            }
          }
        }
      }
    }    
  }

  wrappers {
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
}
