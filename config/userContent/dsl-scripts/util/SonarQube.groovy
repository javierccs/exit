import jenkins.model.*
import javaposse.jobdsl.dsl.jobs.*

def addSonarQubeAnalysis(job, Map<String,String> props = [:]) {
  job.configure {
    it / buildWrappers / 'hudson.plugins.sonar.SonarBuildWrapper'
    if (job instanceof MavenJob) {
      it / postbuilders / 'hudson.tasks.Maven' {
        targets ('$SONAR_MAVEN_GOAL $SONAR_EXTRA_PROPS')
        properties ('sonar.host.url=$SONAR_HOST_URL\nsonar.login=$SONAR_LOGIN\nsonar.password=$SONAR_PASSWORD\n'+
                    'sonar.jdbc.url=$SONAR_JDBC_URL\nsonar.jdbc.username=$SONAR_JDBC_USERNAME\nsonar.jdbc.password=$SONAR_JDBC_PASSWORD'+
                    " " + props.collect { /$it.key=$it.value/ }.join(" "))
        settings (class: "org.jenkinsci.plugins.configfiles.maven.job.MvnSettingsProvider") {
          settingsConfigId ('org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig')
        }
        globalSettings (class: "jenkins.mvn.DefaultGlobalSettingsProvider")
      }
    } else if (job instanceof FreeStyleJob) {
      it / builders / 'hudson.plugins.sonar.SonarRunnerBuilder' {
        properties ('sonar.sourceEncoding=UTF-8\n'+props.collect { /$it.key=$it.value/ }.join("\n"))
        jdk('JDK8')
      }
    } else {
      println "[WARNING] Job type not supported for Sonarqube Analysis;"
      return;
    } 
  }
}

return this;
