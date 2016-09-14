import jenkins.model.*
import java.util.regex.*;
import util.Utilities;

def GITLAB_PROJECT_SOURCE = "${GITLAB_PROJECT_SOURCE}".trim()


job(buildJobName) {
    println "JOB: ${buildJobName}"
    label('')
    logRotator(daysToKeep = 30, numToKeep = 10, artifactDaysToKeep = -1, artifactNumToKeep = -1)
    println("GitLab URL: " + GITLAB_PROJECT_SOURCE);
}