import jenkins.model.*

println "setting up mail"
def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()
jenkinsLocationConfiguration.setAdminAddress(System.getenv("JENKINS_EMAIL"))
jenkinsLocationConfiguration.save()
def inst = Jenkins.getInstance()

// mailer plugin
def mail = inst.getDescriptor("hudson.tasks.Mailer")
mail.setReplyToAddress(System.getenv("JENKINS_EMAIL"))
mail.setSmtpHost(System.getenv("SMTP_HOST"))
mail.setSmtpPort(System.getenv("SMTP_PORT"))
mail.save()

// email-ext plugin
def ext = inst.getDescriptorByType(hudson.plugins.emailext.ExtendedEmailPublisherDescriptor)
ext.smtpHost = System.getenv("SMTP_HOST")
ext.smtpPort = System.getenv("SMTP_PORT")
ext.defaultReplyTo = System.getenv("JENKINS_EMAIL")
ext.defaultContentType = "text/html"
ext.save()
