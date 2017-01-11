import jenkins.model.*
import java.util.regex.*;
import util.Utilities;

// Shared functions
def gitlabHooks = evaluate(new File("$JENKINS_HOME/userContent/dsl-scripts/util/GitLabWebHooks.groovy"))

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def GIT_BRANCH = "${GIT_BRANCH}".trim()
def GITLAB_CREDENTIAL = "${GITLAB_CREDENTIAL}"

// Static values
//checks gitlab url
def gitLabMap = Utilities.parseGitlabUrl(GITLAB_PROJECT);
def GROUP_NAME = gitLabMap.groupName
def REPOSITORY_NAME = gitLabMap.repositoryName
def GITLAB_URL = gitLabMap.url
def gitLabConnectionMap = Utilities.getGitLabConnection ("Serenity GitLab")
def GITLAB_SERVER = gitLabConnectionMap.url;
def GITLAB_API_TOKEN = gitLabConnectionMap.credential.getApiToken().toString();
out.println("GitLab URL: " + GITLAB_URL);
out.println("GitLab Group: " + GROUP_NAME);
out.println("GitLab Project: " + REPOSITORY_NAME);

GITLAB_PROJECT = GROUP_NAME + '/' + REPOSITORY_NAME
def buildJobName = GITLAB_PROJECT+'-ci-build'

//nexus information
def nexusRepositoryUrl = System.getenv('NEXUS_URL') ?: 'srnalmdes202.eng.gsnetcloud.corp:8080'
def apkReleaseRepository = System.getenv('NEXUS_APK_RELEASES') ?: 'android-releases'
def nexus_protocol = 'http'
def nexus_version = 'nexus3'
def apk_type = 'apk'
def nexus_credentialId='maven-deployer-credentials-id'

//source artifact properties
def ARTIFACT_VERSION = "${ARTIFACT_VERSION}".trim()
def ARTIFACT_NAME = "${ARTIFACT_NAME}".trim()
def REPOSITORY_STATIC_NAME = "web"
def GROUP_PROJECT_GITLAB ="${GROUP_PROJECT_GITLAB}".trim()

//target apk properties
def APK_GROUPID = "${APK_GROUPID}".trim()
def APK_ARTIFACTID = "${APK_ARTIFACTID}".trim()
def APK_VERSION = "${APK_VERSION}".trim()

def URL_source_download = nexus_protocol+'://'+nexusRepositoryUrl+'/repository/'+REPOSITORY_STATIC_NAME+'/'+GROUP_PROJECT_GITLAB+'/'+ARTIFACT_NAME+'-'+ARTIFACT_VERSION+'.zip'
def WORKSPACE = '/home/jenkins/workspace/'+buildJobName 

//creck gitlab credentials
def gitlabCredsType = Utilities.getCredentialType(GITLAB_CREDENTIAL)
if ( gitlabCredsType == null ) {
  throw new IllegalArgumentException("ERROR: GitLab credentials ( GITLAB_CREDENTIAL ) not provided! ")
}
println ("GitLab credential type " + gitlabCredsType );


job (buildJobName) {
  println "JOB: "+buildJobName
  label('cordova')
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
    stringParam('gitlabSourceBranch', GIT_BRANCH,
                'Gitlab source branch (only for MERGE events from forked repositories)')
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
      includeBranches(GIT_BRANCH)
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
}//wrappers

 steps {
      shell ('curl ' + URL_source_download + ' -O')
  }

 steps {
    shell('unzip ' + WORKSPACE +'/'+ ARTIFACT_NAME+'-'+ARTIFACT_VERSION+'.zip -d ' + WORKSPACE+ '/www/')
    shell('cordova platform list;cordova plugin remove cordova-plugin-sandkOCRManager;cordova build android')
  }// steps

 steps {
          nexusArtifactUploader {
            nexusVersion(nexus_version)
            protocol(nexus_protocol)
            nexusUrl(nexusRepositoryUrl)
            groupId(APK_GROUPID)
            artifact {
                      artifactId(APK_ARTIFACTID)
                      type(apk_type)
                      classifier('')
                      file(WORKSPACE + '/platforms/android/build/outputs/apk/android-debug.apk')
            }
            version(APK_VERSION)
            repository(apkReleaseRepository)
            credentialsId(nexus_credentialId)
          }
        }

} //job


gitlabHooks.GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, buildJobName)
