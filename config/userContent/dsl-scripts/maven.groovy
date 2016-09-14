import jenkins.model.*
import java.util.regex.*;
import util.Utilities;

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))
def utils = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/Utils.groovy"))

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_INTEGRATION_BRANCH = "${GIT_INTEGRATION_BRANCH}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def GITLAB_CREDENTIAL = "${GITLAB_CREDENTIAL}"
def OSE3_URL ="${OSE3_URL}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim()
// APP_name for OSE3 -it doesnt allow uppercase chars!!
def APP_NAME_OSE3="${APP_NAME_OSE3}".trim().toLowerCase()

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
def buildJobName = GITLAB_PROJECT+'-ci-build'
def BridgeHPALMJobName = GITLAB_PROJECT+'-pre-hpalm-bridge'
def BridgeHPALMJobNameDEV = GITLAB_PROJECT+'-dev-hpalm-bridge'
def deployDevJobName = GITLAB_PROJECT+'-ose3-dev-deploy'
def deployPreJobName = GITLAB_PROJECT+'-ose3-pre-deploy'
def deployProJobName = GITLAB_PROJECT+'-ose3-pro-deploy'
def nexusRepositoryUrl = System.getenv('NEXUS_BASE_URL') ?: 'https://nexus.ci.gsnet.corp/nexus'
def mavenGroupRepository = System.getenv('NEXUS_MAVEN_GROUP') ?: '/content/groups/public/'
def mavenReleaseRepository = System.getenv('NEXUS_MAVEN_RELEASES') ?: '/content/repositories/releases/'
def mavenSnapshotRepository = System.getenv('NEXUS_MAVEN_SNAPSHOTS') ?: '/content/repositories/snapshots/'

if(APP_NAME_OSE3 == "")
  APP_NAME_OSE3=REPOSITORY_NAME.toLowerCase()

//HPALM INFO
def ADD_HPALM_AT_DEV = "${ADD_HPALM_AT_DEV}".trim()
def ADD_HPALM_AT_PRE = "${ADD_HPALM_AT_PRE}".trim()
def _HPALM_TEST_SET_ID_ = "${HPALM_TEST_SET_ID}".trim()
def _HPALM_DOMAIN_ = "${HPALM_DOMAIN}".trim()
def _HPALM_PROJECT_ = "${HPALM_PROJECT}".trim()
def _HPALM_URL_ = "${HPALM_URL}".trim()
def _HPALM_CREDS_ = "${HPALM_CREDS}".trim()
def GITLAB_PROJECT_TEST = "${GITLAB_PROJECT_TEST}".trim()
def URL_BASE_SELENIUM= "${URL_BASE_SELENIUM}".trim()

//JAVASE TEMPLATE VARS
def OTHER_OSE3_TEMPLATE_PARAMS =""
JAVA_OPTS_EXT="${JAVA_OPTS_EXT}".trim()
JAVA_PARAMETERS="${JAVA_PARAMETERS}".trim()
POD_MAX_MEM="${POD_MAX_MEM}".trim()
TZ="${TZ}".trim()
WILY_MOM_FQDN="${WILY_MOM_FQDN}".trim()
WILY_MOM_PORT="${WILY_MOM_PORT}".trim()
//Compose the template params, if blank we left the default pf PAAS
if(JAVA_OPTS_EXT != "") OTHER_OSE3_TEMPLATE_PARAMS+=",JAVA_OPTS_EXT="+JAVA_OPTS_EXT
if(JAVA_PARAMETERS != "") OTHER_OSE3_TEMPLATE_PARAMS+=",JAVA_PARAMETERS="+JAVA_PARAMETERS
if(POD_MAX_MEM != "") OTHER_OSE3_TEMPLATE_PARAMS+=",POD_MAX_MEM="+POD_MAX_MEM
if(TZ != "") OTHER_OSE3_TEMPLATE_PARAMS+=",TZ="+TZ
if(WILY_MOM_FQDN != "") OTHER_OSE3_TEMPLATE_PARAMS+=",WILY_MOM_FQDN="+WILY_MOM_FQDN
if(WILY_MOM_PORT != "") OTHER_OSE3_TEMPLATE_PARAMS+=",WILY_MOM_PORT="+WILY_MOM_PORT

//SONARQUBE
String NAME="Serenity SonarQube"
def sqd = Jenkins.getInstance().getDescriptor("hudson.plugins.sonar.SonarPublisher")
boolean sq = (sqd != null) && sqd.getInstallations().find {NAME.equals(it.getName())}


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



mavenJob (buildJobName) {
  println "JOB: "+buildJobName
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
            trigger(deployPreJobName) {
              parameters {
                predefinedProps(['POM_GROUPID':'${POM_GROUPID}','POM_ARTIFACTID':'${POM_ARTIFACTID}','POM_PACKAGING':'${POM_PACKAGING}','PIPELINE_VERSION':'${POM_VERSION}'])
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
      includeBranches(GIT_INTEGRATION_BRANCH)
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

    buildName('${ENV,var="POM_DISPLAYNAME"}:${ENV,var="POM_VERSION"}-${BUILD_NUMBER}')
    release {
      preBuildSteps {
        environmentVariables {
          env('IS_RELEASE', 'true')
        }
      } 
      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","POM_GROUPID","POM_ARTIFACTID","POM_PACKAGING","POM_VERSION"]')
        }
      }
      postSuccessfulBuildSteps {
        shell("git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}")
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
    shell("if [ \"\${IS_RELEASE}\" = true ]; then git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}; fi")
  }

  goals('clean verify')
    // Use managed global Maven settings.
  providedSettings('Serenity Maven Settings')
  mavenOpts('-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true')

  postBuildSteps {
    if (sq) {
      maven {
        goals('$SONAR_MAVEN_GOAL $SONAR_EXTRA_PROPS')
        providedSettings('Serenity Maven Settings')
        properties('sonar.host.url': '$SONAR_HOST_URL','sonar.jdbc.url': '$SONAR_JDBC_URL',
                   'sonar.login': '$SONAR_LOGIN', 'sonar.password': '$SONAR_PASSWORD',
                   'sonar.jdbc.username': '$SONAR_JDBC_USERNAME', 'sonar.jdbc.password': '$SONAR_JDBC_PASSWORD')
      }
    }
  }

  publishers {
    deployArtifacts {
      repositoryId('serenity')
      repositoryUrl(nexusRepositoryUrl+mavenSnapshotRepository)
      uniqueVersion(true)
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
                predefinedProp('OSE3_URL', OSE3_URL)
                predefinedProp('OSE3_APP_NAME', APP_NAME_OSE3)
                predefinedProp('OSE3_TEMPLATE_NAME','javase')
                predefinedProps(['POM_GROUPID':'${POM_GROUPID}','POM_ARTIFACTID':'${POM_ARTIFACTID}','POM_PACKAGING':'${POM_PACKAGING}','PIPELINE_VERSION':'${POM_VERSION}'])
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
    if (sq) {it/buildWrappers/'hudson.plugins.sonar.SonarBuildWrapper' (plugin: "sonar@2.4.4")}
    it/publishers/'hudson.maven.RedeployPublisher'/releaseEnvVar('IS_RELEASE')
  }
} //job

def updateParam(node, String paramName, String defaultValue) {
  def aux = node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'*'.find {
    it.name != null && it.name.text() == paramName
  }
  aux.defaultValue[0].value = defaultValue
}

def removeParam(node, String paramName) {
  def aux = node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions.'*'.find {
    it.name.text() == paramName
  }
  node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions[0].remove(aux)
}

/// HPALM JOBS ///
if (ADD_HPALM_AT_DEV == "true") {
mavenJob(BridgeHPALMJobNameDEV) {
  println "JOB: ${BridgeHPALMJobNameDEV}"
  using('TJ-hpalm-test')
  disabled(false)
  deliveryPipelineConfiguration('DEV', 'Functional Test (DEV)')
  scm {
    git {
      branch(GIT_INTEGRATION_BRANCH)
      browser {
        gitLab(GITLAB_SERVER+ (GITLAB_PROJECT_TEST ?: GITLAB_PROJECT), '8.6')
      } //browser
      remote {
        credentials(GITLAB_CREDENTIAL)
        name('origin')
        url(GITLAB_URL+ (GITLAB_PROJECT_TEST ?: GITLAB_PROJECT) +'.git')
      } //remote
    } //git
  } //scm
  wrappers {
    credentialsBinding {
      usernamePassword('CREDENTIALS', _HPALM_CREDS_)
    }
  }
  configure {
    updateParam(it, 'HPALM_URL', _HPALM_URL_)
    updateParam(it, 'HPALM_PROJECT', _HPALM_PROJECT_)
    updateParam(it, 'HPALM_TEST_SET_ID', _HPALM_TEST_SET_ID_)
    updateParam(it, 'HPALM_DOMAIN', _HPALM_DOMAIN_)
  }
} //mavenJob
}//HPALM BRIDGE DEV

//HPALM Bridge PRE
if(ADD_HPALM_AT_PRE == "true") {
mavenJob(BridgeHPALMJobName) {
  println "JOB: ${BridgeHPALMJobName}"
  using('TJ-hpalm-test')
  disabled(false)
  deliveryPipelineConfiguration('PRE', 'Functional Test')
  scm {
    git {
      branch(GIT_INTEGRATION_BRANCH)
      browser {
        gitLab(GITLAB_SERVER + (GITLAB_PROJECT_TEST ?: GITLAB_PROJECT), '8.6')
      } //browser
      remote {
        credentials(GITLAB_CREDENTIAL)
        name('origin')
        url(GITLAB_URL + (GITLAB_PROJECT_TEST ?: GITLAB_PROJECT) +'.git')
      } //remote
    } //git
  } //scm
  wrappers {
    credentialsBinding {
      usernamePassword('CREDENTIALS', _HPALM_CREDS_)
    }
  }
  configure {
    updateParam(it, 'HPALM_URL', _HPALM_URL_)
    updateParam(it, 'HPALM_PROJECT', _HPALM_PROJECT_)
    updateParam(it, 'HPALM_TEST_SET_ID', _HPALM_TEST_SET_ID_)
    updateParam(it, 'HPALM_DOMAIN', _HPALM_DOMAIN_)
  }
} //mavenJob
}//HPALM BRIDGE PRE

/// DEPLOY JOBS ///
def shellnode =
  "<hudson.tasks.Shell>" +
  "  <command>" +
  'export ARTIFACT_URL=$(mvn_resolve.sh ${POM_GROUPID} ${POM_ARTIFACTID} ${PIPELINE_VERSION} ${POM_PACKAGING})\n'+
  'echo \"OSE3_TEMPLATE_PARAMS=APP_NAME=$OSE3_APP_NAME,ARTIFACT_URL=$ARTIFACT_URL'+ OTHER_OSE3_TEMPLATE_PARAMS + '\" > ${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties'+
  "  </command>"+
  "</hudson.tasks.Shell>"
def envnode =
  '<EnvInjectBuilder><info><propertiesFilePath>'+
  '${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties'+
  '</propertiesFilePath></info></EnvInjectBuilder>'

//Deploy in dev job
job (deployDevJobName) {
  println "JOB: " + deployDevJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('DEV', 'Deploy')
  parameters {
    stringParam('POM_GROUPID', '', 'Maven artifact Group ID')
    stringParam('POM_ARTIFACTID', '', 'Maven artifact ID')
    stringParam('POM_PACKAGING', 'jar', 'Maven artifact packaging type')
  }
  publishers {
    if (ADD_HPALM_AT_DEV == "true") {
      downstreamParameterized {
        trigger(BridgeHPALMJobNameDEV) {
          condition('SUCCESS')
          parameters {
            predefinedProp('seleniumBaseURL','${OSE3_END_POINT_URL}' )
          }
        }
      }
    } //HPALM
  }
  configure {
    removeParam(it, 'OSE3_TEMPLATE_PARAMS')
    updateParam(it, 'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-dev')
    updateParam(it, 'OSE3_APP_NAME',  APP_NAME_OSE3)
    updateParam(it, 'OSE3_TEMPLATE_NAME','javase')
    updateParam(it,'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_DEV)
    (it / builders).children().add(0, new XmlParser().parseText(envnode))
    (it / builders).children().add(0, new XmlParser().parseText(shellnode))
  }  
}

//Deploy in pre job
job (deployPreJobName) {
  println "JOB: " + deployPreJobName
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('PRE', 'Deploy')
  parameters {
    stringParam('POM_GROUPID', '', 'Maven artifact Group ID')
    stringParam('POM_ARTIFACTID', '', 'Maven artifact ID')
    stringParam('POM_PACKAGING', 'jar', 'Maven artifact packaging type')
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
                predefinedProps(['POM_GROUPID':'${POM_GROUPID}','POM_ARTIFACTID':'${POM_ARTIFACTID}','POM_PACKAGING':'${POM_PACKAGING}','PIPELINE_VERSION':'${PIPELINE_VERSION}'])
              }
            }
          }
        }
      }
    }
  }
  publishers {
    if (ADD_HPALM_AT_PRE == "true") {
      downstreamParameterized {
        trigger(BridgeHPALMJobName) {
          condition('SUCCESS')
          parameters {
            predefinedProp('seleniumBaseURL','${OSE3_END_POINT_URL}' )
          }
        }
      }
    } //HPALM
  }
  configure {
    removeParam(it, 'OSE3_TEMPLATE_PARAMS')
    updateParam(it, 'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
    updateParam(it, 'OSE3_APP_NAME',  APP_NAME_OSE3)
    updateParam(it, 'OSE3_TEMPLATE_NAME','javase')
    updateParam(it,'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_PRE)
    (it / builders).children().add(0, new XmlParser().parseText(envnode))
    (it / builders).children().add(0, new XmlParser().parseText(shellnode))
  }
}

//Deploy in pro job
job (deployProJobName) {
  println "JOB: $deployProJobName"
  using('TJ-ose3-deploy')
  disabled(false)
  deliveryPipelineConfiguration('PRO', 'Deploy')
  parameters {
    stringParam('POM_GROUPID', '', 'Maven artifact Group ID')
    stringParam('POM_ARTIFACTID', '', 'Maven artifact ID')
    stringParam('POM_PACKAGING', 'jar', 'Maven artifact packaging type')
  }
  configure {
    removeParam(it, 'OSE3_TEMPLATE_PARAMS')
    updateParam(it, 'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pro')
    updateParam(it, 'OSE3_APP_NAME',  APP_NAME_OSE3)
    updateParam(it, 'OSE3_TEMPLATE_NAME','javase')
    updateParam(it,'OSE3_TOKEN_PROJECT',OSE3_TOKEN_PROJECT_PRO)
    (it / builders).children().add(0, new XmlParser().parseText(envnode))
    (it / builders).children().add(0, new XmlParser().parseText(shellnode))
  }
}

gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, buildJobName)
