import jenkins.model.*
import java.util.regex.*
import util.Utilities
import util.AuthorizationJobFactory
import util.OSE3DeployJobFactory

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))
def sonarqube = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/SonarQube.groovy"))

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_INTEGRATION_BRANCH = "${GIT_INTEGRATION_BRANCH}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def GITLAB_CREDENTIAL = "${GITLAB_CREDENTIAL}"


def deployData = [:]
def hpAlmData = [:]
try {
  deployData['OSE3_URL'] = "${OSE3_URL}".trim()
  out.println ("OSE3_URL: " + deployData['OSE3_URL'])
} catch (e){
  // library jar. No OSE3 deploy
  out.println ("'OSE3_URL' variable not set. Pipeline won't generate deployment jobs")
}

//checks gitlab url
def gitLabMap = Utilities.parseGitlabUrl(GITLAB_PROJECT);
def GROUP_NAME = gitLabMap.groupName
def REPOSITORY_NAME = gitLabMap.repositoryName
def GITLAB_URL = gitLabMap.url
def gitLabConnectionMap = Utilities.getGitLabConnection ("Serenity GitLab")
def GITLAB_SERVER = gitLabConnectionMap.url;
def GITLAB_API_TOKEN = gitLabConnectionMap.credential.getApiToken().toString();



if ( deployData['OSE3_URL'] ) {
  deployData['OSE3_PROJECT_NAME'] = "${OSE3_PROJECT_NAME}".trim()
  // APP_name for OSE3 -it doesnt allow uppercase chars!!
  deployData['APP_NAME_OSE3'] ="${APP_NAME_OSE3}".trim().toLowerCase()
  // if true generates blue green deployment jobs
  deployData['blueGreenDeployment'] = OSE3_BLUE_GREEN_DEPLOYMENT.toBoolean()
  if( deployData['APP_NAME_OSE3'] == "") {
    out.println ("APP_NAME_OSE3 variable not set. "  +
      REPOSITORY_NAME.toLowerCase() + " will be used as OSE3 application name")
    deployData['APP_NAME_OSE3'] = REPOSITORY_NAME.toLowerCase()
  }
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

  deployData['OTHER_OSE3_TEMPLATE_PARAMS'] = OTHER_OSE3_TEMPLATE_PARAMS

  //TOKEN_OSE3
  deployData['OSE3_TOKEN_PROJECT_DEV'] = "${OSE3_TOKEN_PROJECT_DEV}".trim()
  deployData['OSE3_TOKEN_PROJECT_PRE'] = ""
  deployData['OSE3_TOKEN_PROJECT_PRO'] = ""


  // HPALM INFO
  hpAlmData['ADD_HPALM_AT_DEV'] = "${ADD_HPALM_AT_DEV}".trim()
  hpAlmData['ADD_HPALM_AT_PRE'] = "${ADD_HPALM_AT_PRE}".trim()
  hpAlmData['_HPALM_TEST_SET_ID_'] = "${HPALM_TEST_SET_ID}".trim()
  hpAlmData['_HPALM_DOMAIN_'] = "${HPALM_DOMAIN}".trim()
  hpAlmData['_HPALM_PROJECT_'] = "${HPALM_PROJECT}".trim()
  hpAlmData['_HPALM_URL_'] = "${HPALM_URL}".trim()
  hpAlmData['_HPALM_CREDS_'] = "${HPALM_CREDS}".trim()
  hpAlmData['GITLAB_PROJECT_TEST'] = "${GITLAB_PROJECT_TEST}".trim()
  hpAlmData['URL_BASE_SELENIUM'] = "${URL_BASE_SELENIUM}".trim()

} // end if ( deployData['OSE3_URL'] ) {

out.println("GitLab URL: " + GITLAB_URL);
out.println("GitLab Group: " + GROUP_NAME);
out.println("GitLab Project: " + REPOSITORY_NAME);

GITLAB_PROJECT = GROUP_NAME + '/' + REPOSITORY_NAME
def nexusRepositoryUrl = System.getenv('NEXUS_BASE_URL') ?: 'https://nexus.alm.gsnetcloud.corp'
def mavenReleaseRepository = System.getenv('NEXUS_MAVEN_RELEASES') ?: '/content/repositories/releases/'
def mavenSnapshotRepository = System.getenv('NEXUS_MAVEN_SNAPSHOTS') ?: '/content/repositories/snapshots/'

def buildJobName = GITLAB_PROJECT+'-ci-build'

def deployJobNames = [:]
if ( deployData['OSE3_URL'] ) {
  deployJobNames['BridgeHPALMJobName'] = GITLAB_PROJECT+'-pre-hpalm-bridge'
  deployJobNames['BridgeHPALMJobNameDEV'] = GITLAB_PROJECT+'-dev-hpalm-bridge'
  deployJobNames['deployDevJobName'] = GITLAB_PROJECT+'-ose3-dev-deploy'
  deployJobNames['deployPreCheckJobName'] = GITLAB_PROJECT+'-ose3-pre-check-deploy'
  deployJobNames['deployProCheckJobName'] = GITLAB_PROJECT+'-ose3-pro-check-deploy'
  deployJobNames['deployPreJobName'] = GITLAB_PROJECT+'-ose3-pre-deploy'
  deployJobNames['deployHideJobName'] = OSE3DeployJobFactory.getHideJobName(GITLAB_PROJECT)
  deployJobNames['deployProJobName'] = OSE3DeployJobFactory.getProJobName(deployData['blueGreenDeployment'], GITLAB_PROJECT)
}


//creck gitlab credentials
def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL, GITLAB_URL)
if ( gitlabCredsType == null ) {
  throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
}
out.println ("GitLab credential type " + gitlabCredsType );

def buildJob = mavenJob (buildJobName) {
  out.println "JOB: "+buildJobName
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
// if not library jar
if ( deployData['OSE3_URL'] ) {
    promotions{
      promotion {
        name('DEV')
        icon('star-blue')
        conditions {
          downstream(false, deployJobNames['deployDevJobName'])
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
            trigger(deployJobNames['deployPreCheckJobName']) {
              parameters {
                predefinedProps([
                  'POM_GROUPID':'${POM_GROUPID}',
                  'POM_ARTIFACTID':'${POM_ARTIFACTID}',
                  'POM_PACKAGING':'${POM_PACKAGING}',
                  'PIPELINE_VERSION':'${POM_VERSION}'])
              }
            }  //trigger deployPreCheckJobName
          }  // end downstreamParameterized
        } // end actions pre-check
      } // end promotion

      promotion {
        name('PRE')
        icon('star-silver-w')
        conditions {
          downstream(false, deployJobNames['deployPreJobName'])
        }
      }
if ( deployData['blueGreenDeployment'] ) {
      promotion {
        name('Shadow')
        icon('star-gold-w')
        conditions {
          downstream(false, deployJobNames['deployHideJobName'])
        }
     }
}  // blueGreenDeployment
      //in pro
      promotion {
        name('PRO')
        icon('star-gold')
        conditions {
          downstream(false, deployJobNames['deployProJobName'])
        }
      }
      //in pro
    }
} // end not library jar?
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
//If user password credentials are provided bind is required
if ( gitlabCredsType == 'UserPassword' ){
    credentialsBinding {
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

  postBuildSteps('UNSTABLE') {
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
// checks if it is a library jar
if ( deployData['OSE3_URL'] ) {
        publishers {
          downstreamParameterized {
            trigger(deployJobNames['deployDevJobName']) {
              condition('SUCCESS')
              parameters {
                predefinedProp('OSE3_URL', deployData['OSE3_URL'])
                predefinedProp('OSE3_APP_NAME', deployData['APP_NAME_OSE3'])
                predefinedProp('OSE3_TEMPLATE_NAME','javase')
                predefinedProps(['POM_GROUPID':'${POM_GROUPID}','POM_ARTIFACTID':'${POM_ARTIFACTID}','POM_PACKAGING':'${POM_PACKAGING}','PIPELINE_VERSION':'${POM_VERSION}'])
              }
            }
          }
        } // end publishers not release
} // end not library jar
      }  // end conditional action not release
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
} //job

//SONARQUBE
String NAME="Serenity SonarQube"
def sqd = Jenkins.getInstance().getDescriptor("hudson.plugins.sonar.SonarGlobalConfiguration")
boolean sq = (sqd != null) && sqd.getInstallations().find {NAME.equals(it.getName())}
if (sq) sonarqube.addSonarQubeAnalysis(buildJob)

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
  if (aux != null)  node.properties.'hudson.model.ParametersDefinitionProperty'.parameterDefinitions[0].remove(aux)
}

// only generates hp alm and deploy jobs if it is not a libray jar

if ( deployData['OSE3_URL'] ) {
  /// HPALM JOBS ///
  if ( hpAlmData['ADD_HPALM_AT_DEV'] == "true") {
  mavenJob(deployJobNames['BridgeHPALMJobNameDEV']) {
    out.println("JOB: " + deployJobNames['BridgeHPALMJobNameDEV'])
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
        usernamePassword('CREDENTIALS', hpAlmData['_HPALM_CREDS_'])
      }
    }
    configure {
      updateParam(it, 'HPALM_URL', hpAlmData['_HPALM_URL_'])
      updateParam(it, 'HPALM_PROJECT', hpAlmData['_HPALM_PROJECT_'])
      updateParam(it, 'HPALM_TEST_SET_ID', hpAlmData['_HPALM_TEST_SET_ID_'])
      updateParam(it, 'HPALM_DOMAIN', hpAlmData['_HPALM_DOMAIN_'])
    }
  } //mavenJob
  }//HPALM BRIDGE DEV

  //HPALM Bridge PRE
  if( hpAlmData['ADD_HPALM_AT_PRE'] == "true") {
  mavenJob(deployJobNames['BridgeHPALMJobName']) {
    out.println("JOB: " + deployJobNames['BridgeHPALMJobName'])
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
        usernamePassword('CREDENTIALS', hpAlmData['_HPALM_CREDS_'])
      }
    }
    configure {
      updateParam(it, 'HPALM_URL', hpAlmData['_HPALM_URL_'])
      updateParam(it, 'HPALM_PROJECT', hpAlmData['_HPALM_PROJECT_'])
      updateParam(it, 'HPALM_TEST_SET_ID', hpAlmData['_HPALM_TEST_SET_ID_'])
      updateParam(it, 'HPALM_DOMAIN', hpAlmData['_HPALM_DOMAIN_'])
    }
  } //mavenJob
  }//HPALM BRIDGE PRE

  /// DEPLOY JOBS ///
  def shellnode =
    "<hudson.tasks.Shell>" +
    "  <command>" +
    'export ARTIFACT_URL=$(mvn_resolve.sh ${POM_GROUPID} ${POM_ARTIFACTID} ${PIPELINE_VERSION} ${POM_PACKAGING})\n'+
    'echo \"OSE3_TEMPLATE_PARAMS=APP_NAME=' + deployData['APP_NAME_OSE3'] + ',ARTIFACT_URL=$ARTIFACT_URL'+ deployData['OTHER_OSE3_TEMPLATE_PARAMS'] + '\" > ${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties'+
    "  </command>"+
    "</hudson.tasks.Shell>"
  def envnode =
    '<EnvInjectBuilder><info><propertiesFilePath>'+
    '${WORKSPACE}/NEXUS_URL_${BUILD_NUMBER}.properties'+
    '</propertiesFilePath></info></EnvInjectBuilder>'

  def approvalJobArgs = [
    ['POM_GROUPID', '', 'Maven artifact Group ID'],
    ['POM_ARTIFACTID', '', 'Maven artifact ID'],
    ['POM_PACKAGING', 'jar', 'Maven artifact packaging type'],
    ['PIPELINE_VERSION', '', 'Pipeline version']
  ]
  //Deploy in dev job
  job (deployJobNames['deployDevJobName']) {
    out.println "JOB: " + deployJobNames['deployDevJobName']
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
          trigger(deployJobNames['BridgeHPALMJobNameDEV']) {
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
      removeParam(it, 'CERTIFICATE')
      removeParam(it, 'PRIVATE_KEY_CERTIFICATE')
      removeParam(it, 'CA_CERTIFICATE')
      updateParam(it, 'OSE3_URL', deployData['OSE3_URL'])
      updateParam(it, 'OSE3_PROJECT_NAME', deployData['OSE3_PROJECT_NAME']+'-dev')
      updateParam(it, 'OSE3_APP_NAME',  deployData['APP_NAME_OSE3'])
      updateParam(it, 'OSE3_TEMPLATE_NAME','javase')
      updateParam(it,'OSE3_TOKEN_PROJECT', deployData['OSE3_TOKEN_PROJECT_DEV'])
      (it / builders).children().add(0, new XmlParser().parseText(envnode))
      (it / builders).children().add(0, new XmlParser().parseText(shellnode))
    }
  }

  // pre approval job
  AuthorizationJobFactory.createApprovalJob(this,
    deployJobNames['deployPreCheckJobName'], false, '${POM_ARTIFACTID}:${PIPELINE_VERSION}',
    approvalJobArgs, deployJobNames['deployPreJobName'])

  //Deploy in pre job
  job (deployJobNames['deployPreJobName']) {
    out.println "JOB: " + deployJobNames['deployPreJobName']
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
          name('PRO-Check')
          icon('star-purple')
          conditions {
            manual(Utilities.getPrePromotionRoleGroups())
          }
          actions {
            downstreamParameterized {
              trigger(deployJobNames['deployProCheckJobName']) {
                parameters {
                  predefinedProps([
                    'POM_GROUPID':'${POM_GROUPID}',
                    'POM_ARTIFACTID':'${POM_ARTIFACTID}',
                    'POM_PACKAGING':'${POM_PACKAGING}',
                    'PIPELINE_VERSION':'${PIPELINE_VERSION}'])
                }
              }  //trigger deployPreCheckJobName
            }  // end downstreamParameterized
          } // end actions pre-check
        } // en promotion

      }
    }
    publishers {
      if (ADD_HPALM_AT_PRE == "true") {
        downstreamParameterized {
          trigger(deployJobNames['BridgeHPALMJobNameDEV']) {
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
      removeParam(it, 'CERTIFICATE')
      removeParam(it, 'PRIVATE_KEY_CERTIFICATE')
      removeParam(it, 'CA_CERTIFICATE')
      updateParam(it, 'OSE3_URL', deployData['OSE3_URL'])
      updateParam(it, 'OSE3_PROJECT_NAME', deployData['OSE3_PROJECT_NAME'] +'-pre')
      updateParam(it, 'OSE3_APP_NAME',  deployData['APP_NAME_OSE3'])
      updateParam(it, 'OSE3_TEMPLATE_NAME','javase')
      updateParam(it,'OSE3_TOKEN_PROJECT', deployData['OSE3_TOKEN_PROJECT_PRE'])
      (it / builders).children().add(0, new XmlParser().parseText(envnode))
      (it / builders).children().add(0, new XmlParser().parseText(shellnode))
    }
  }

  // pro deployment Jobs
  OSE3DeployJobFactory.createOse3ProJobs (this, deployData['blueGreenDeployment'],
    '${POM_ARTIFACTID}:${PIPELINE_VERSION}',
      approvalJobArgs, GITLAB_PROJECT,
      deployData['OSE3_URL'], deployData['OSE3_PROJECT_NAME'] + '-pro', deployData['APP_NAME_OSE3'], 'javase', null,
      [envnode, shellnode]
      )
} // end library jar
gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, buildJobName)
