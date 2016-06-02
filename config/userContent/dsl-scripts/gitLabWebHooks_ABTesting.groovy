/*
  GITLAB_PROJECT: GitLab project name, (like groupname/repositoryname)
*/
import jenkins.model.*

// Input parameters
def GITLAB_PROJECT = "${GITLAB_PROJECT}".trim()
def INITIAL_JOB_NAME_FEATURE_A = GITLAB_PROJECT+'-ci-build-feature-A'
def INITIAL_JOB_NAME_FEATURE_B = GITLAB_PROJECT+'-ci-build-feature-B'

def gitlab = Jenkins.getInstance().getDescriptor("com.dabsquared.gitlabjenkins.GitLabPushTrigger")
def GITLAB_SERVER = gitlab.getGitlabHostUrl()
def GITLAB_API_TOKEN = gitlab.getGitlabApiToken()

def url = new URL(GITLAB_SERVER+"/api/v3/projects/"+java.net.URLEncoder.encode(GITLAB_PROJECT)+"/hooks?private_token="+GITLAB_API_TOKEN)
def connection = url.openConnection()
connection.setRequestMethod("GET")
connection.doOutput = true
connection.connect()

def text = connection.content.text
assert connection.responseCode == 200

webhook = Jenkins.getInstance().getRootUrl()+"project/"+INITIAL_JOB_NAME_FEATURE_A
if (!text.contains("url\":\""+webhook+"\"")) {
    url = new URL(GITLAB_SERVER+"/api/v3/projects/"+java.net.URLEncoder.encode(GITLAB_PROJECT)+"/hooks?private_token="+GITLAB_API_TOKEN+"&url="+webhook+
      "&merge_requests_events=false&push_events=true")
  connection = url.openConnection()
  connection.setRequestMethod("POST")
  connection.doOutput = true
  connection.connect()
  assert connection.responseCode == 201
  println "New hook: "+webhook
}

webhook = Jenkins.getInstance().getRootUrl()+"project/"+INITIAL_JOB_NAME_FEATURE_B
if (!text.contains("url\":\""+webhook+"\"")) {
    url = new URL(GITLAB_SERVER+"/api/v3/projects/"+java.net.URLEncoder.encode(GITLAB_PROJECT)+"/hooks?private_token="+GITLAB_API_TOKEN+"&url="+webhook+
      "&merge_requests_events=false&push_events=true")
  connection = url.openConnection()
  connection.setRequestMethod("POST")
  connection.doOutput = true
  connection.connect()
  assert connection.responseCode == 201
  println "New hook: "+webhook
}
