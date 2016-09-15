import jenkins.model.*
import java.util.regex.*;

def GITLAB_PROJECT_SOURCE = "${GITLAB_PROJECT_SOURCE}"
def GITLAB_PROJECT_TARGET = "${GITLAB_PROJECT_TARGET}"
def GITLAB_USER = "${GITLAB_USER}"
def GITLAB_PASSWORD = "${GITLAB_PASSWORD}"

out.println("Running groovy");

job(buildJobName) {
    //label('')
    logRotator(daysToKeep = 30, numToKeep = 10, artifactDaysToKeep = -1, artifactNumToKeep = -1)
    steps {
        shell("migrate_gitlab_from_v0_to_v1.sh ${GITLAB_PROJECT_SOURCE} ${GITLAB_PROJECT_TARGET} ${GITLAB_USER} ${GITLAB_PASSWORD}");
    }
}