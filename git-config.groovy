import jenkins.model.*

println "-->Setting up git"
def inst = Jenkins.getInstance()

def desc = inst.getDescriptor("hudson.plugins.git.GitSCM")

desc.setGlobalConfigName("jenkins")
desc.setGlobalConfigEmail("jenkins@serenity.corp")

desc.save()
