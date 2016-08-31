#!/bin/bash

#Starts fluentd if fluentd server variable is set
if [ -z "$SERENITY_FLUENTD_SERVER" ]; then
    echo "Warning: Serenity fluentd server variable (SERENITY_FLUENTD_SERVER) is unset. td-agent won't be started"
else
    #Changes jenkins log file to be followed by td-agent
    export JENKINS_LOG_FILE=/var/log/td-agent/jenkins.log
    echo Setting jenkins log file to $JENKINS_LOG_FILE
    export JENKINS_OPTS="$JENKINS_OPTS --logfile=$JENKINS_LOG_FILE"
    #Checks if tenant variable has been set
	if [ -z "$SERENITY_TENANT" ]; then
		echo "Warning: Serenity tenant (SERENITY_TENANT) is unset. Default value (default-tenant) will be used"
		export SERENITY_TENANT=default-tenant
	fi
	if [ -z "$SERENITY_FLUENTD_PORT" ]; then
	   echo "Warning: Serenity fluentd port variable (SERENITY_FLUENTD_PORT) is unset. Default port 24224 will be used"
	   export SERENITY_FLUENTD_PORT=24224
	fi
	echo "Starting td-agent..."
	/etc/init.d/td-agent start
fi
#Starts jenkins
echo "Including serenity-alm styles"
THEME_TMP_DIR=/tmp/theme
mkdir $THEME_TMP_DIR
export com_serenity_image_version=$(env | grep version | cut -d '=' -f 2)
echo "Serenity ALM Jenkins version: $com_serenity_image_version"
cp -R /opt/theme/* $THEME_TMP_DIR
if [ -f /tmp/theme/$SERENITYALM_CSS.template  ];
then
  envsubst < /tmp/theme/$SERENITYALM_CSS.template > /tmp/theme/$SERENITYALM_CSS
  # template is deleted
  rm $THEME_TMP_DIR/$SERENITYALM_CSS.template
fi
if [ -f /tmp/theme/$SERENITYALM_JS.template  ];
then
  envsubst < /tmp/theme/$SERENITYALM_JS.template > /tmp/theme/$SERENITYALM_JS
  # template is deleted
  rm $THEME_TMP_DIR/$SERENITYALM_JS.template
fi


cp /usr/share/jenkins/jenkins.war /tmp/jenkins.war
current=$PWD
cd $THEME_TMP_DIR
zip -ur  /tmp/jenkins.war css images scripts
cp /tmp/jenkins.war  /usr/share/jenkins/jenkins.war
cd $current
rm -rf $THEME_TMP_DIR

echo "Starting jenkins..."
/bin/tini -- /usr/local/bin/jenkins.sh
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
