/*
  GITLAB_SERVER: The complete URL to the Gitlab server
  GITLAB_API_TOKEN: API Token for accessing Gitlab
  GITLAB_PROJECT: GitLab project name, like groupname/repositoryname
  INITIAL_JOB_NAME: First job in the pipeline. Usually the build/compile job
*/
import jenkins.model.*

def GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, INITIAL_JOB_NAME) {
  try{
    def url = new URL(GITLAB_SERVER+"api/v3/projects/"+java.net.URLEncoder.encode(GITLAB_PROJECT)+"/hooks?private_token="+GITLAB_API_TOKEN)
    def connection = url.openConnection()
    connection.setRequestMethod("GET")
    connection.doOutput = true
    connection.connect()

    def text = connection.content.text
    assert connection.responseCode == 200

    webhook = Jenkins.getInstance().getRootUrl()+"project/"+INITIAL_JOB_NAME
    if (!text.contains("url\":\""+webhook+"\"")) {
        url = new URL(GITLAB_SERVER+"api/v3/projects/"+java.net.URLEncoder.encode(GITLAB_PROJECT)+"/hooks?private_token="+GITLAB_API_TOKEN+"&url="+webhook+
      "&merge_requests_events=false&push_events=true")
        connection = url.openConnection()
        connection.setRequestMethod("POST")
        connection.doOutput = true
        connection.connect()
        assert connection.responseCode == 201
        println "New hook: "+webhook
    }
  }catch (java.io.FileNotFoundException e){
    throw new javaposse.jobdsl.dsl.DslException ("[ERROR] Project " + GITLAB_PROJECT + " does not exist in " + GITLAB_SERVER + " or it is unreachable.")
  
  }
}

return this;
