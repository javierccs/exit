#!/bin/bash

#Required environment variables validation
: "${JENKINS_URL?ERROR: Required variable is not set. Try running the container with \'-e JENKINS_URL=url\'}"
: "${JENKINS_EMAIL?ERROR: Required variable is not set. Try running the container with \'-e JENKINS_EMAIL=email_address\'}"
: "${GITLAB_URL?ERROR: Required variable is not set. Try running the container with \'-e GITLAB_URL=url\'}"
: "${GITLAB_API_TOKEN?ERROR: Required variable is not set. Try running the container with \'-e GITLAB_API_TOKEN=token\'}"
: "${SWARM_MASTER_URL?ERROR: Required variable is not set. Try running the container with \'-e SWARM_MASTER_URL=url\'}"
: "${MAVEN_DEPLOYER_LOGIN?ERROR: Required variable is not set. Try running the container with \'-e MAVEN_DEPLOYER_LOGIN=user\'}"
: "${MAVEN_DEPLOYER_PASSWD?ERROR: Required variable is not set. Try running the container with \'-e MAVEN_DEPLOYER_PASSWD=password\'}"
: "${DOCKER_REGISTRY_USERNAME?ERROR: Required variable is not set. Try running the container with \'-e DOCKER_REGISTRY_USERNAME=user\'}"
: "${DOCKER_REGISTRY_PASSWORD?ERROR: Required variable is not set. Try running the container with \'-e DOCKER_REGISTRY_PASSWORD=pass\'}"
: "${DOCKER_SLAVES_VOLUMES_ROOT?ERROR: Required variable is not set. Try running the container with \'-e DOCKER_SLAVES_VOLUMES_ROOT=path\'}"
: "${NEXUS_BASE_URL?ERROR: Required variable is not set. Try running the container with \'-e NEXUS_BASE_URL=url\'}"

#Starts fluentd if fluentd server variable is set
#installs certificates
. /usr/local/bin/install-certificates.sh
#Includes jenkins styles
. /usr/local/bin/include-styles.sh

#Jenkins war will be modified by entrypoint to add serenity-alm.css
chown jenkins:jenkins /usr/share/jenkins/jenkins.war
#Starts jenkins with jenkins user
su jenkins /usr/local/bin/jenkins-start.sh
echo Error starting jenkins!
if [ -z "$SERENITY_FLUENTD_SERVER" ]; then
  echo "Done."
else
  #prints jenkins log file
  echo Jenkins log file
  echo ###############
  cat $JENKINS_LOG_FILE  
  echo ###############
fi
