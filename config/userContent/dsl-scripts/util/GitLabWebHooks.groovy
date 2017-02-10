/*
  GITLAB_SERVER: The complete URL to the Gitlab server
  GITLAB_API_TOKEN: API Token for accessing Gitlab
  GITLAB_PROJECT: GitLab project name, like groupname/repositoryname
  INITIAL_JOB_NAME: First job in the pipeline. Usually the build/compile job
*/
import jenkins.model.*
import java.net.URLEncoder;
import groovy.json.JsonSlurper

def GitLabWebHooks(GITLAB_SERVER, GITLAB_API_TOKEN, GITLAB_PROJECT, INITIAL_JOB_NAME) {
    try {
        def gitlabProject = "" ;
        if ( GITLAB_PROJECT.indexOf(".") >= 0 ) {
            gitlabProject =  getProjectId(GITLAB_SERVER, GITLAB_PROJECT, GITLAB_API_TOKEN);
        } else {
            gitlabProject = URLEncoder.encode(GITLAB_PROJECT)
        }

        def aux = GITLAB_SERVER + "api/v3/projects/" + gitlabProject + "/hooks?private_token=" + GITLAB_API_TOKEN ;
        def url = new URL( aux )
        def connection = url.openConnection()

        connection.setRequestMethod("GET")
        connection.doOutput = true
        connection.connect()

        def text = connection.content.text
        assert connection.responseCode == 200

        webhook = Jenkins.getInstance().getRootUrl()+"project/"+INITIAL_JOB_NAME

        if (!text.contains("url\":\""+webhook+"\"")) {
            url = new URL(GITLAB_SERVER + "api/v3/projects/" + gitlabProject + "/hooks?private_token=" + GITLAB_API_TOKEN + "&url=" + webhook + 
                  "&merge_requests_events=false&push_events=true&enable_ssl_verification=false"); 
            connection = url.openConnection()
            connection.setRequestMethod("POST")
            connection.doOutput = true
            connection.connect()
            assert connection.responseCode == 201
            println "New hook: "+webhook
        }
    } catch (java.io.FileNotFoundException e){
        throw new javaposse.jobdsl.dsl.DslException ("[ERROR1] Project " + GITLAB_PROJECT + " does not exist in " + GITLAB_SERVER + " or it is unreachable.")
    } catch (java.net.MalformedURLException e){
        throw new javaposse.jobdsl.dsl.DslException ("[ERROR2] Project " + GITLAB_PROJECT + " is not a valid project name, or " + GITLAB_SERVER + " is not properly configured.")
    } catch (java.io.IOException e){
        throw new javaposse.jobdsl.dsl.DslException ("[ERROR3] Error creating webhook for " + GITLAB_PROJECT + " project in  " + GITLAB_SERVER + ". Verify if server is reachable and if its credentials are valid." )
    }
}

def getProjectInfoFromGitLab(gitlabServer, gitlabProject, gitlabApiToken) {
    def url = new URL( gitlabServer + "api/v3/projects/" + URLEncoder.encode(gitlabProject) + "?private_token=" + gitlabApiToken  );
    def connection = url.openConnection()
    connection.setRequestMethod("GET")
    connection.doOutput = true
    connection.connect()
    def text = connection.content.text
    
    def slurper = new JsonSlurper();
    def result = slurper.parseText(text);
    
    return result;
}

def getProjectId(gitlabServer, gitlabProject, gitlabApiToken) {
    def projectInfo = getProjectInfoFromGitLab(gitlabServer, gitlabProject, gitlabApiToken);
    if ( projectInfo == null || projectInfo.id == null ){
        throw new java.io.FileNotFoundException ("project not found.")
    } else {
        return projectInfo.id;
    }
}

return this;
