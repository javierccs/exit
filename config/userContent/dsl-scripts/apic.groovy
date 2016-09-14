import jenkins.model.*
import groovy.util.*
import util.Utilities;


// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_INTEGRATION_BRANCH = "${GIT_INTEGRATION_BRANCH}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def SERENITY_CREDENTIAL = "${SERENITY_CREDENTIAL}"
def APIC_CREDENTIAL = "${APIC_CREDENTIAL}"
def APIC_SERVER = "${APIC_SERVER}".trim()
def APIC_ORGANIZATION = "${APIC_ORGANIZATION}".trim().toLowerCase()
def APIC_DEV_CATALOG = "${APIC_DEV_CATALOG}".trim().toLowerCase()
def APIC_PRE_CATALOG = "${APIC_PRE_CATALOG}".trim().toLowerCase()
def APIC_PRO_CATALOG = "${APIC_PRO_CATALOG}".trim().toLowerCase()


// Static values
def gitlab = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
def GITLAB_SERVER = gitlab.getGitlabHostUrl()
def (GROUP_NAME, REPOSITORY_NAME) = GITLAB_PROJECT.tokenize('/')
def buildJobName = GITLAB_PROJECT+'-ci-build'
def publishDevJobName = GITLAB_PROJECT+'-ose3-dev-publish'
def publishPreJobName = GITLAB_PROJECT+'-ose3-pre-publish'
def publishProJobName = GITLAB_PROJECT+'-ose3-pro-publish'

//creck gitlab credentials
def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL)
if ( gitlabCredsType == null ) {
  throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
}
println ("GitLab credential type " + gitlabCredsType );


job (buildJobName) {
  println "JOB: "+buildJobName
  label('apic')
  deliveryPipelineConfiguration('CI', 'APIC Validation')
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
          manual('impes-product-owner,impes-technical-lead,impes-developer')
        }
        actions {
          downstreamParameterized {
            trigger(publishPreJobName) {
              parameters {
                predefinedProp('PIPELINE_VERSION','${APIC_IMAGE_VERSION}')
              }
            }
          }
        }
      }
      promotion {
        name('DEV')
        icon('star-gold')
        conditions {
          downstream(false, publishDevJobName)
        }
      }
      promotion {
        name('PRE')
        icon('star-gold-w')
        conditions {
          downstream(false, publishPreJobName)
        }
      }
      promotion {
        name('PRO')
        icon('star-gold')
        conditions {
          downstream(false, publishProJobName)
        }
      }
    }
  }

  scm {
    git {
      branch('${gitlabSourceRepoName}/${gitlabSourceBranch}')
      browser {
        gitLab(GITLAB_SERVER+'/'+GITLAB_PROJECT, '8.6')
      } //browser
      remote {
        credentials(SERENITY_CREDENTIAL)
        name('origin')
        url(GITLAB_SERVER+'/'+GITLAB_PROJECT+'.git')
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
//If user password credentials are provided bind is required
if ( gitlabCredsType == 'UserPassword' ){
          usernamePassword('GITLAB_CREDENTIAL', GITLAB_CREDENTIAL)
}
//if ssh credentials ssAgent is added
if ( gitlabCredsType == 'SSH' ){
      sshAgent(GITLAB_CREDENTIAL)
}
    buildName("$GROUP_NAME" + ':${ENV,var="FRONT_IMAGE_VERSION"}-${BUILD_NUMBER}')
    release {
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","APIC_IMAGE_VERSION"]')
        }
      }
      postSuccessfulBuildSteps {
        shell("git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}")
      }
      preBuildSteps {
        environmentVariables {
          env('IS_RELEASE',true) 
        }
      }
    } //release
  }
  steps {
    shell("if [ \"\${IS_RELEASE}\" = true ]; then git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}; fi")
    shell(
	     ' echo "INFO Validating project yamls"...\n'  +
	     " for f in $APIC_SRC_DIRECTORY/*_Product_*.yaml; do\n" +
         '   apic validate "$f"\n' +
         ' done\n ' +
		 ' currentDir=$(pwd)\n' +
         " cd $APIC_SRC_DIRECTORY \n" +
         ' tar -czf $currentDir/apic.tgz *.yaml \n' +
         ' cd $currentDir \n')
  }
  publishers {
    archiveArtifacts('*.tgz')
    extendedEmail('$DEFAULT_RECIPIENTS', '$DEFAULT_SUBJECT', '${JELLY_SCRIPT, template="static-analysis.jelly"}') {
      trigger(triggerName: 'Always')
      trigger(triggerName: 'Failure', includeCulprits: true)
      trigger(triggerName: 'Unstable', includeCulprits: true)
      trigger(triggerName: 'FixedUnhealthy', sendToDevelopers: true)
      configure {
        it/contentType('text/html')
      }
    } //extendedEmail
	
    flexiblePublish {
      conditionalAction {
        condition { not {
            booleanCondition('${ENV,var="IS_RELEASE"}')
          }
        }
        publishers {
          downstreamParameterized {
            trigger(publishDevJobName) {
              condition('SUCCESS')
              parameters {
                predefinedProp('PIPELINE_VERSION', '${PIPELINE_VERSION}')
              }
            }
          }
        }
      }
    }
	
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


job (publishDevJobName) {
  println "JOB: "+publishDevJobName
  label('apic')
  deliveryPipelineConfiguration('DEV', 'APIC Publish')
  parameters {
    stringParam('ARTIFACT_NAME', 'apic.tgz', 'APIC artifact name')
	stringParam('APIC_SERVER', "$APIC_SERVER")
	stringParam('APIC_ORGANIZATION', "$APIC_ORGANIZATION")
	stringParam('APIC_CATALOG', "$APIC_DEV_CATALOG")
  }
  wrappers {
    buildName('${ENV,var="PIPELINE_VERSION_TEST"}-${BUILD_NUMBER}')
    credentialsBinding {
      usernamePassword('APIC_USERNAME','APIC_PASSWORD', APIC_CREDENTIAL)
    }
  }
  steps {
    copyArtifacts(buildJobName) {
      includePatterns('apic.tgz')
      flatten()
      optional(false)
      fingerprintArtifacts(false)
      buildSelector {
        latestSuccessful(true)
      }
    }
    shell('deploy_in_apimanager.sh')

	
  }
}
                                

//Publish in pre job
job (publishPreJobName) {
  println "JOB: " + publishPreJobName
  disabled(false)
  deliveryPipelineConfiguration('PRE', 'APIC Publish')
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
            trigger(publishProJobName) {
              parameters {
                predefinedProp('PIPELINE_VERSION','${PIPELINE_VERSION}')
              }
            }
          }
        }
      }
    }
  }
}

//Deploy in pro job
job (publishProJobName) {
  println "JOB: $publishProJobName"
  disabled(false)
  deliveryPipelineConfiguration('PRO', 'APIC Publish')
}
