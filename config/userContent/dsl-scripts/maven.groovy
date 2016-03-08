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

// Build job
mavenJob (buildJobName) {
  println "JOB: "+buildJobName
  label('maven')
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
          manual('') {
          }
        }
        actions {
          downstreamParameterized {
            trigger(deployPreJobName,'SUCCESS') {
              parameters {
                predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
                predefinedProp('OSE3_CREDENTIAL', SERENITY_CREDENTIAL)
                predefinedProp('OSE3_APP_NAME', REPOSITORY_NAME)
                predefinedProp('OSE3_TEMPLATE_NAME',"${OSE3_TEMPLATE_NAME}".trim())
                predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS}".trim())
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
    buildName('${ENV,var="POM_DISPLAYNAME"}-${ENV,var="POM_VERSION"}-${BUILD_NUMBER}')
    release {
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
                tagName('${WORDPRESS_IMAGE_VERSION}')
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
  }

  goals('clean verify')

  publishers {
    deployArtifacts {
      repositoryId('repositoryId')
      repositoryUrl('snapshotRepositoryUrl')
      uniqueVersion(true)
    }
    downstreamParameterized {
      trigger(deployDevJobName) {
        condition('SUCCESS')
        parameters {
          predefinedProp('OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-dev')
          predefinedProp('OSE3_CREDENTIAL', SERENITY_CREDENTIAL)
          predefinedProp('OSE3_APP_NAME', REPOSITORY_NAME)
          predefinedProp('OSE3_TEMPLATE_NAME',"${OSE3_TEMPLATE_NAME}".trim())
          predefinedProp('OSE3_TEMPLATE_PARAMS',"${OSE3_TEMPLATE_PARAMS}".trim())
        }
      }
    }
  } //publishers
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

//Deploy in pre job
job (deployPreJobName) {
  println "JOB: " + deployPreJobName
  label('ose3-deploy')
  deliveryPipelineConfiguration('PRE', 'Deploy')
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
    shell('deploy_in_ose3.sh')
  }
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
