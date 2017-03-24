#Executes all this commands must be executed with jenkins user
echo "[INFO] Starting jenkins..."
/bin/tini -- /usr/local/bin/jenkins.sh
echo "[ERROR] Error starting jenkins!"
  #prints jenkins log file
echo "[INFO] Jenkins log file"
echo ###############
cat $JENKINS_LOG_FILE
echo ###############

