import jenkins.model.*

println "-->Setting up git"
def inst = Jenkins.getInstance()
def git = inst.getDescriptor("hudson.plugins.git.GitSCM")
def email = (System.getenv("JENKINS_EMAIL") == null)? "jenkins@serenity.corp":System.getenv("JENKINS_EMAIL")
git.setGlobalConfigName("jenkins")
git.setGlobalConfigEmail(email)
git.save()

println "-->Setting up GitLab"
def gitlab = inst.getDescriptorByType(com.dabsquared.gitlabjenkins.GitLabPushTrigger.DescriptorImpl)
gitlab.gitlabApiToken = System.getenv("GITLAB_API_TOKEN")
gitlab.gitlabHostUrl = System.getenv("GITLAB_URL")
gitlab.ignoreCertificateErrors = true
gitlab.save()
