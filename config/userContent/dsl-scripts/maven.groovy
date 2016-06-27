import jenkins.model.*

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_INTEGRATION_BRANCH = "${GIT_INTEGRATION_BRANCH}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def SERENITY_CREDENTIAL = "${SERENITY_CREDENTIAL}"
def OSE3_URL ="${OSE3_URL}".trim()
def OSE3_PROJECT_NAME = "${OSE3_PROJECT_NAME}".trim()
// APP_name for OSE3 -it doesnt allow uppercase chars!!
def APP_NAME_OSE3="${APP_NAME_OSE3}".trim().toLowerCase()
if(APP_NAME_OSE3 == "")
 APP_NAME_OSE3=REPOSITORY_NAME.toLowerCase()

// Static values
def gitlab = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
def GITLAB_SERVER = gitlab.getGitlabHostUrl()
def (GROUP_NAME, REPOSITORY_NAME) = GITLAB_PROJECT.tokenize('/')
def buildJobName = GITLAB_PROJECT+'-ci-build'
def BridgeHPALMJobName = GITLAB_PROJECT+'-pre-hpalm-bridge'
def BridgeHPALMJobNameDEV = GITLAB_PROJECT+'-dev-hpalm-bridge'
def deployDevJobName = GITLAB_PROJECT+'-ose3-dev-deploy'
def deployPreJobName = GITLAB_PROJECT+'-ose3-pre-deploy'
def deployProJobName = GITLAB_PROJECT+'-ose3-pro-deploy'
def nexusRepositoryUrl = System.getenv('NEXUS_BASE_URL')
if (nexusRepositoryUrl==null) {
  nexusRepositoryUrl='https://nexus.ci.gsnet.corp/nexus'
}

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

mavenJob (buildJobName) {
  println "JOB: "+buildJobName
  label('maven')
  deliveryPipelineConfiguration('CI', 'Build&Package')
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
            trigger(deployPreJobName) {
              parameters {
                predefinedProp('VALUE_URL',nexusRepositoryUrl + '/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=releases')
                predefinedProp('PIPELINE_VERSION','${POM_VERSION}')
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
      usernamePassword('GITLAB_CREDENTIAL', SERENITY_CREDENTIAL)
      usernamePassword('OSE3_USERNAME','OSE3_PASSWORD', SERENITY_CREDENTIAL)
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
          binding('ENV_LIST', '["IS_RELEASE","POM_GROUPID","POM_ARTIFACTID","POM_VERSION"]')
        }
      }
      postSuccessfulBuildSteps {
        shell("git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}")
      }
      configure {
        it / 'postSuccessfulBuildSteps' << 'hudson.maven.RedeployPublisher' {
          id('serenity')
          url(nexusRepositoryUrl+'/content/repositories/releases')
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
  mavenOpts('-Dmaven.wagon.http.ssl.insecure=true')
  mavenOpts('-Dmaven.wagon.http.ssl.allowall=true')
  mavenOpts('-Dmaven.wagon.http.ssl.ignore.validity.dates=true')

  postBuildSteps {
    if (sq) {
      maven {
        goals('$SONAR_MAVEN_GOAL $SONAR_EXTRA_PROPS')
        providedSettings('Serenity Maven Settings')
        properties('sonar.host.url': '$SONAR_HOST_URL','sonar.jdbc.url': '$SONAR_JDBC_URL', 'sonar.analysis.mode': 'preview',
                   'sonar.login': '$SONAR_LOGIN', 'sonar.password': '$SONAR_PASSWORD',
                   'sonar.jdbc.username': '$SONAR_JDBCUSERNAME', 'sonar.jdbc.password': '$SONAR_JDBC_PASSWORD')
      }
    }
  }

  publishers {
    deployArtifacts {
      repositoryId('serenity')
      repositoryUrl(nexusRepositoryUrl+'/content/repositories/snapshots')
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
                predefinedProp('OSE3_USERNAME','${OSE3_USERNAME}')
                predefinedProp('OSE3_PASSWORD','${OSE3_PASSWORD}')
                predefinedProp('VALUE_URL',nexusRepositoryUrl + '/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=snapshots')
                predefinedProp('PIPELINE_VERSION','${POM_VERSION}')
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
    if (sq) {it/buildWrappers/'hudson.plugins.sonar.SonarBuildWrapper' (plugin: "sonar@2.3")}
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
        gitLab(GITLAB_SERVER+'/'+ (GITLAB_PROJECT_TEST ?: GITLAB_PROJECT), '8.6')
      } //browser
      remote {
        credentials(SERENITY_CREDENTIAL)
        name('origin')
        url(GITLAB_SERVER+'/'+ (GITLAB_PROJECT_TEST ?: GITLAB_PROJECT) +'.git')
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
        gitLab(GITLAB_SERVER+'/'+ (GITLAB_PROJECT_TEST ?: GITLAB_PROJECT), '8.6')
      } //browser
      remote {
        credentials(SERENITY_CREDENTIAL)
        name('origin')
        url(GITLAB_SERVER+'/'+ (GITLAB_PROJECT_TEST ?: GITLAB_PROJECT) +'.git')
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
  'export ARTIFACT_URL=$(curl -k -s -I $VALUE_URL -I | awk \'/Location: (.*)/ {print $2}\' | tail -n 1 | tr -d \'\\r\')\n'+
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
    stringParam('VALUE_URL', '', '')
  }
  wrappers {
    credentialsBinding {
      usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', SERENITY_CREDENTIAL)
    }
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
    stringParam('VALUE_URL', '', '')
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
                predefinedProp('VALUE_URL','${VALUE_URL}')
                predefinedProp('PIPELINE_VERSION','${PIPELINE_VERSION}')
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
    stringParam('VALUE_URL', '', '')
  }
  configure {
    removeParam(it, 'OSE3_TEMPLATE_PARAMS')
    updateParam(it, 'OSE3_URL', OSE3_URL)
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pro')
    updateParam(it, 'OSE3_APP_NAME',  APP_NAME_OSE3)
    updateParam(it, 'OSE3_TEMPLATE_NAME','javase')
    (it / builders).children().add(0, new XmlParser().parseText(envnode))
    (it / builders).children().add(0, new XmlParser().parseText(shellnode))
  }
}
