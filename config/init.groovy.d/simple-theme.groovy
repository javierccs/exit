import jenkins.model.*
import org.codefirst.SimpleThemeDecorator 
def inst = Jenkins.getInstance()
def themeDecorator = inst.getDescriptor("org.codefirst.SimpleThemeDecorator")
def jurl = System.getenv("JENKINS_URL").trim();

//Sets Serenity ALM theme
if ( !jurl.endsWith('/') ) {
  jurl += '/';
}
if (  !System.getenv("SERENITYALM_CSS").trim().isEmpty() ) {
  themeDecorator.cssUrl = jurl + System.getenv("SERENITYALM_CSS").trim();
}
if (  !System.getenv("SERENITYALM_JS").trim().isEmpty() ) {
  themeDecorator.jsUrl = jurl + System.getenv("SERENITYALM_JS").trim();
}

themeDecorator.save()
