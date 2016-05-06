import jenkins.model.*
import groovy.util.*

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_INTEGRATION_BRANCH = "${GIT_INTEGRATION_BRANCH}".trim()
def GIT_RELEASE_BRANCH = "${GIT_RELEASE_BRANCH}".trim()
def OSE3_URL ="${OSE3_URL}".trim()
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

//JAVASE TEMPLATE VARS
def OTHER_OSE3_TEMPLATE_PARAMS =""
// JAVA_OPTS_EXT="${JAVA_OPTS_EXT}".trim()
TZ="${TZ}".trim()

//Compose the template params, if blank we left the default pf PAAS
if(TZ != "") OTHER_OSE3_TEMPLATE_PARAMS+=",TZ="+TZ

//SONARQUBE
String NAME="Serenity SonarQube"
def sqd = Jenkins.getInstance().getDescriptor("hudson.plugins.sonar.SonarPublisher")
boolean sq = (sqd != null) && sqd.getInstallations().find {NAME.equals(it.getName())}

job (buildJobName) {
  println "JOB: "+buildJobName
  label('nodejs')
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
          manual('impes-product-owner,impes-technical-lead,impes-developer') {}
        }
        actions {
          downstreamParameterized {
            trigger(deployPreJobName,'SUCCESS') {
              parameters {
                predefinedProp('OSE3_URL', OSE3_URL)
                predefinedProp('OSE3_APP_NAME', GITLAB_PROJECT)
                predefinedProp('OSE3_TEMPLATE_NAME','nginx')
                predefinedProp('OSE3_TEMPLATE_PARAMS','OSE3_TEMPLATE_PARAMS=APP_NAME=$OSE3_APP_NAME,ARTIFACT_URL=$nexusRepositoryUrl/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=releases'+OTHER_OSE3_TEMPLATE_PARAMS)
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
    credentialsBinding {
      usernamePassword('OSE3_USERNAME', 'OSE3_PASSWORD', SERENITY_CREDENTIAL)
    }
    buildName('${ENV,var="POM_DISPLAYNAME"}:${ENV,var="POM_VERSION"}-${BUILD_NUMBER}')
    release {
/*      postBuildSteps {
        systemGroovyCommand(readFileFromWorkspace('dsl-scripts/util/InjectBuildParameters.groovy')) {
          binding('ENV_LIST', '["IS_RELEASE","POM_GROUPID","POM_ARTIFACTID","POM_VERSION"]')
        }
      }*/
      postSuccessfulBuildSteps {
        shell('git-flow-release-finish.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}')
      }
      preBuildSteps {
        shell('git-flow-release-start.sh ${GIT_INTEGRATION_BRANCH} ${GIT_RELEASE_BRANCH}')
      }
    } //release
  }

  steps {
    shell('export http_proxy=http://proxyapps.gsnet.corp:80\n'+
          'export https_proxy=http://proxyapps.gsnet.corp:80\n'+
          'npm install\n'+
          'bower install\n'+
          'grunt\n'+
          'tar -czvf test-project.tgz html')
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
    archiveArtifacts('*.tgz')
    flexiblePublish {
      conditionalAction{
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
                 predefinedProp('OSE3_USERNAME', '${OSE3_USERNAME}')
                 predefinedProp('OSE3_PASSWORD', '${OSE3_PASSWORD}')
                 predefinedProp('OSE3_APP_NAME', APP_NAME_OSE3)
                 predefinedProp('OSE3_TEMPLATE_NAME','nginx')
                 predefinedProp('OSE3_TEMPLATE_PARAMS','OSE3_TEMPLATE_PARAMS=APP_NAME=$OSE3_APP_NAME,ARTIFACT_URL=$nexusRepositoryUrl/service/local/artifact/maven/redirect?g=${POM_GROUPID}&a=${POM_ARTIFACTID}&v=${POM_VERSION}&r=snapshots'+OTHER_OSE3_TEMPLATE_PARAMS)
               }
             }
           }
         }
       } //conditionalAction
    } // flexiblePublish
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

  configure {
    if (sq) {it/buildWrappers/'hudson.plugins.sonar.SonarBuildWrapper' (plugin: "sonar@2.3")}
  }
} //job

def shellnode = 
  "<hudson.tasks.Shell>" +
  "  <command>" +
  'export ARTIFACT_URL=$(curl -k -s -I $VALUE_URL -I | awk \'/Location: (.*)/ {print $2}\' | tail -n 1 | tr -d \'\\r\')'+
  "  </command>"+
  "</hudson.tasks.Shell>"

def updateParam(node, String paramName, String defaultValue) {
  def aux = node.depthFirst().find { it.name.text() == paramName }
  aux.defaultValue[0].value = defaultValue
}
                                
//Deploy in dev job
job (deployDevJobName) {
  println "JOB: " + deployDevJobName
  using('TL-seed-deploy')
  disabled(false)
  deliveryPipelineConfiguration('DEV', 'Deploy')
  configure {
    (it / builders).children().add(0, new XmlParser().parseText(shellnode))
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-dev')
  }
}

//Deploy in pre job
job (deployPreJobName) {
  println "JOB: " + deployPreJobName
  using('TL-seed-deploy')
  disabled(false)
  deliveryPipelineConfiguration('PRE', 'Deploy')
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
                predefinedProp('OSE3_URL', '${OSE3_URL}')
                predefinedProp('OSE3_APP_NAME', '${OSE3_APP_NAME}')
                predefinedProp('OSE3_TEMPLATE_NAME','${OSE3_TEMPLATE_NAME}')
                predefinedProp('OSE3_TEMPLATE_PARAMS','${OSE3_TEMPLATE_PARAMS}')
                predefinedProp('PIPELINE_VERSION','${PIPELINE_VERSION}')
              }
            }
          }
        }
      }
    }
  }
  configure {
    (it / builders).children().add(0, new XmlParser().parseText(shellnode))
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pre')
  }
}

//Deploy in pro job
job (deployProJobName) {
  println "JOB: $deployProJobName"
  using('TL-seed-deploy')
  disabled(false)
  deliveryPipelineConfiguration('PRO', 'Deploy')
  configure {
    (it / builders).children().add(0, new XmlParser().parseText(shellnode))
    updateParam(it, 'OSE3_PROJECT_NAME', OSE3_PROJECT_NAME+'-pro')
  }
}
