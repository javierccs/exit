import jenkins.model.*
import groovy.json.JsonSlurper
import util.Utilities
import util.AuthorizationJobFactory
import util.OSE3DeployJobFactory

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))

def updateParam(node, String paramName, String defaultValue) {
  def aux = node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'*'.find {
    it.name != null && it.name.text() == paramName
  }
  assert aux != null : "Param name '$paramName' not found in node '$node'"
  aux.defaultValue[0].value = defaultValue
}

//Deploy in dev job
def removeParam(node, String paramName) {
  def aux = node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'*'.find {
    it.name.text() == paramName
  }
  node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions[0].remove(aux)
}

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GITLAB_CREDENTIAL = "${GITLAB_CREDENTIAL}"
def GIT_INTEGRATION_BRANCH = "${GIT_INTEGRATION_BRANCH}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def slurper = new JsonSlurper()
def buildProps = slurper.parseText("${FRONT_BUILD}".trim())
def ose3props = slurper.parseText("${OPENSHIFT3}".trim())
def OSE3_TOKEN_PROJECT_DEV="${OSE3_TOKEN_PROJECT_DEV}".trim()

//checks gitlab url
def webRepository = System.getenv('WEB_REPOSITORY')
assert webRepository != null: "[SEVERE] WEB_REPOSITORY env variable not found."
//checks openshift params
assert ose3props.name?.trim() : "[ERROR] OpenShift3 project name (OPENSHIFT3.name) not provided! "
assert OSE3_TOKEN_PROJECT_DEV?.trim() : "[ERROR] OpenShift3 dev token (OSE3_TOKEN_PROJECT_DEV) not provided! "
//creck gitlab credentials
def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL)
if ( gitlabCredsType == null ) {
  throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
}
out.println ("GitLab credential type " + gitlabCredsType );
// if true generates blue green deployment jobs
boolean blueGreenDeployment = false
// Static values
def gitLabMap = Utilities.parseGitlabUrl(GITLAB_PROJECT);
def GROUP_NAME = gitLabMap.groupName
def REPOSITORY_NAME = gitLabMap.repositoryName
def GITLAB_URL = gitLabMap.url
def gitLabConnectionMap = Utilities.getGitLabConnection ("Serenity GitLab")
def GITLAB_SERVER = gitLabConnectionMap.url;
def GITLAB_API_TOKEN = gitLabConnectionMap.credential.getApiToken().toString();
GITLAB_PROJECT = GROUP_NAME + '/' + REPOSITORY_NAME
out.println("GitLab URL: " + GITLAB_URL);
out.println("GitLab Group: " + GROUP_NAME);
out.println("GitLab Project: " + REPOSITORY_NAME);

def buildJobName = GITLAB_PROJECT+'-ci-build'
def deployDevJobName = GITLAB_PROJECT+'-ose3-dev-deploy'
def deployPreCheckJobName = GITLAB_PROJECT+'-ose3-pre-check-deploy'
def deployProCheckJobName = GITLAB_PROJECT+'-ose3-pro-check-deploy'
def deployPreJobName = GITLAB_PROJECT+'-ose3-pre-deploy'
def deployHideJobName = OSE3DeployJobFactory.getHideJobName(GITLAB_PROJECT)
def deployProJobName = OSE3DeployJobFactory.getProJobName(blueGreenDeployment, GITLAB_PROJECT)

def COMPILER = buildProps.COMPILER.trim()
def CONFIG_DIRECTORY = buildProps.CONFIG_DIRECTORY.trim()
def DIST_DIR = buildProps.DIST_DIR.trim()
def DIST_INCLUDE = buildProps.DIST_INCLUDE.trim()
def DIST_EXCLUDE= buildProps.DIST_EXCLUDE.trim()

def ARTIFACT_URL = "\${WEB_REGISTRY_DEV}$GITLAB_PROJECT/\$front_image_name-\${FRONT_IMAGE_VERSION}.zip"
def ARTIFACTCONF_URL = "\${WEB_REGISTRY_DEV}$GITLAB_PROJECT/config-\${FRONT_IMAGE_VERSION}.zip"

//OSE3 TEMPLATE VARS
def OSE3_TEMPLATE_PARAMS = ose3props.environments.collect { it.parameters.collectEntries { p -> [p.name, p.value] } }
OSE3_TEMPLATE_PARAMS.each { env ->
  if (!env.APP_NAME?.trim()) env.APP_NAME = REPOSITORY_NAME.toLowerCase()
  if (!CONFIG_DIRECTORY?.trim()) env.ARTIFACTCONF_URL = ''
}

def buildJob = job (buildJobName) {
  out.println "JOB: "+buildJobName
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
                predefinedProp('PIPELINE_VERSION','${FRONT_IMAGE_VERSION}')
                predefinedProp('ARTIFACT_URL',ARTIFACT_URL)
                predefinedProp('ARTIFACTCONF_URL',ARTIFACTCONF_URL)
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
        icon('star-gold-w')
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
        gitLab(GITLAB_SERVER+GITLAB_PROJECT, '8.13')
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
//If user password credentials are provided bind is required
        credentialsBinding {
          usernamePassword('NEXUS_DEPLOYMENT_USERNAME','NEXUS_DEPLOYMENT_PASSWORD', 'maven-deployer-credentials-id')
if ( gitlabCredsType == 'UserPassword' ){
          usernamePassword('GITLAB_CREDENTIAL', GITLAB_CREDENTIAL)
}
        }

//if ssh credentials ssAgent is added
if ( gitlabCredsType == 'SSH' ){
      sshAgent(GITLAB_CREDENTIAL)
}

	buildName( REPOSITORY_NAME + ':${ENV,var="FRONT_IMAGE_VERSION"}')
    release {
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","FRONT_IMAGE_VERSION","front_image_name"]')
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
          envs(IS_RELEASE: true, WEB_REGISTRY_DEV: '${WEB_REGISTRY}')
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
  configure {
    def auxFrontImageName ;
    if (COMPILER.trim().equals ( "None" ) ) {
        auxFrontImageName = REPOSITORY_NAME + "-${BUILD_NUMBER}";
    } else {
        auxFrontImageName = '$FRONT_IMAGE_NAME';
    }

    it / buildWrappers / 'hudson.plugins.sonar.SonarBuildWrapper'
    it / builders / 'hudson.plugins.sonar.SonarRunnerBuilder' {
      properties ('sonar.sourceEncoding=UTF-8\n'+
        ["sonar.sources" : "." , "sonar.exclusions" : "pdf/**, node_modules/**,bower_components/**,${DIST_DIR}/**",
         "sonar.projectKey" : 'serenity:nodejs:' + auxFrontImageName , "sonar.projectName" : auxFrontImageName ,
         "sonar.projectVersion" : '$FRONT_IMAGE_VERSION'].collect { /$it.key=$it.value/ }.join("\n"))
      jdk('JDK8')
    }
  }
  steps {
    shell ("set +x\n"+
           "curl -ku \$NEXUS_DEPLOYMENT_USERNAME:\$NEXUS_DEPLOYMENT_PASSWORD --upload-file ${REPOSITORY_NAME}.zip ${ARTIFACT_URL} || {\n"+
           "  echo \"[ERROR] Failed to deploy ${REPOSITORY_NAME}.zip to url ${ARTIFACT_URL}.\"\n"+
           "  exit 1; }\n" +
           "if [ -f config.zip ]; then\n"+
           "  curl -ku \$NEXUS_DEPLOYMENT_USERNAME:\$NEXUS_DEPLOYMENT_PASSWORD --upload-file config.zip ${ARTIFACTCONF_URL} || {\n"+
           "    echo \"[ERROR] Failed to deploy ${REPOSITORY_NAME}.zip to url ${ARTIFACT_URL}.\"\n"+
           "    exit 1; }\n"+
           "else echo \"[WARN] No config.zip file found!\"; fi\n")
  }

  publishers {
if (buildProps.JUNIT_TESTS_PATTERN?.trim()) {
    archiveJunit(buildProps.JUNIT_TESTS_PATTERN.trim())
}
    flexiblePublish {
      conditionalAction {
        condition { not {
            booleanCondition('${ENV,var="IS_RELEASE"}')
          }
        }
        publishers {
          downstreamParameterized {
            trigger(deployDevJobName) {
              condition('SUCCESS')
              parameters {
                predefinedProp('PIPELINE_VERSION', '${FRONT_IMAGE_VERSION}')
                predefinedProp('ARTIFACT_URL',ARTIFACT_URL)
                predefinedProp('ARTIFACTCONF_URL',ARTIFACTCONF_URL)
              }
            }
          }
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

job (deployDevJobName) {
  out.println "JOB: " + deployDevJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('DEV', 'Deploy')

  parameters {
    stringParam('ARTIFACT_URL', '', '')
    stringParam('ARTIFACTCONF_URL', '', '')
  }

  configure {
    removeParam(it, 'CERTIFICATE')
    removeParam(it, 'PRIVATE_KEY_CERTIFICATE')
    removeParam(it, 'CA_CERTIFICATE')
    updateParam(it, 'OSE3_URL', ose3props.region)
    updateParam(it, 'OSE3_PROJECT_NAME', ose3props.name+'-'+ose3props.environments[0].name)
    updateParam(it, 'OSE3_APP_NAME', OSE3_TEMPLATE_PARAMS[0].APP_NAME)
    updateParam(it, 'OSE3_TEMPLATE_NAME', ose3props.environments[0].template)
    updateParam(it, 'OSE3_TEMPLATE_PARAMS',OSE3_TEMPLATE_PARAMS[0].collect { /$it.key=$it.value/ }.join(","))
    updateParam(it, 'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_DEV)
  }
}

def approvalJobArgs = [
  ['PIPELINE_VERSION','${PIPELINE_VERSION}'],
  ['ARTIFACT_URL','${ARTIFACT_URL}'],
  ['ARTIFACTCONF_URL','${ARTIFACTCONF_URL}']
]
def approvalJobBuildName = REPOSITORY_NAME + ':${ENV,var="FRONT_IMAGE_VERSION"}'
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

  parameters {
    stringParam('ARTIFACT_URL', '', '')
    stringParam('ARTIFACTCONF_URL', '', '')
  }

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
                predefinedProp('ARTIFACT_URL','${ARTIFACT_URL}')
                predefinedProp('ARTIFACTCONF_URL','${ARTIFACTCONF_URL}')
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
    updateParam(it, 'OSE3_URL', ose3props.region)
    updateParam(it, 'OSE3_PROJECT_NAME', ose3props.name+'-'+ose3props.environments[1].name)
    updateParam(it, 'OSE3_APP_NAME', OSE3_TEMPLATE_PARAMS[1].APP_NAME)
    updateParam(it, 'OSE3_TEMPLATE_NAME', ose3props.environments[1].template)
    updateParam(it, 'OSE3_TEMPLATE_PARAMS', OSE3_TEMPLATE_PARAMS[1].collect { /$it.key=$it.value/ }.join(","))
  }
}

// pro approval and deployment Jobs
OSE3DeployJobFactory.createOse3ProJobs (this, blueGreenDeployment,
  deployProCheckJobName,
    approvalJobArgs, GITLAB_PROJECT,
    ose3props.region,
    ose3props.name+'-'+ose3props.environments[2].name,
    OSE3_TEMPLATE_PARAMS[2].APP_NAME,
    ose3props.environments[2].template,
    OSE3_TEMPLATE_PARAMS[2].collect { /$it.key=$it.value/ }.join(","))

gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, buildJobName)
