#!/bin/bash

#Starts fluentd if fluentd server variable is set
if [ -z "$SERENITY_FLUENTD_SERVER" ]; then
    echo "[WARN] Serenity fluentd server variable (SERENITY_FLUENTD_SERVER) is unset. td-agent won't be started"
else
    #Changes jenkins log file to be followed by td-agent
    export JENKINS_LOG_FILE=/var/log/td-agent/jenkins.log
    echo "[INFO] Setting jenkins log file to $JENKINS_LOG_FILE"
    export JENKINS_OPTS="$JENKINS_OPTS --logfile=$JENKINS_LOG_FILE"
    #Checks if tenant variable has been set
	if [ -z "$SERENITY_TENANT" ]; then
		echo "[WARN] Serenity tenant (SERENITY_TENANT) is unset. Default value (default-tenant) will be used"
		export SERENITY_TENANT=default-tenant
	fi
	if [ -z "$SERENITY_FLUENTD_PORT" ]; then
	   echo "[WARN] Serenity fluentd port variable (SERENITY_FLUENTD_PORT) is unset. Default port 24224 will be used"
	   export SERENITY_FLUENTD_PORT=24224
	fi
	echo "[INFO] Starting td-agent..."
	/etc/init.d/td-agent start
fi
