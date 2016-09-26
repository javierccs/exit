#!/bin/bash
#Starts fluentd if fluentd server variable is set
#installs certificates
. /usr/local/bin/install-certificates.sh
#Includes jenkins styles
. /usr/local/bin/include-styles.sh

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
