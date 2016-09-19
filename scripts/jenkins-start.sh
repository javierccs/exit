#Executes all this commands must be executed with jenkins user
if [ -z "$SERENITY_FLUENTD_SERVER" ]; then
    echo "[WARN] Serenity fluentd server variable (SERENITY_FLUENTD_SERVER) is unset. td-agent won't be started"
else
    echo "[INFO] Starting fluentd..."
   . /usr/local/bin/start-fluentd.sh
fi
echo "[INFO] Starting jenkins..."
/bin/tini -- /usr/local/bin/jenkins.sh
echo "[ERROR] Error starting jenkins!"
if [ -z "$SERENITY_FLUENTD_SERVER" ]; then
  echo "[INFO] Done."
else
  #prints jenkins log file
  echo "[INFO] Jenkins log file"
  echo ###############
  cat $JENKINS_LOG_FILE
  echo ###############
fi


