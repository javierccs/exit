#Install certificates (if necesary) in Jenkins
#from configuration URLs

CA_CERTS_DIRECTORY=/usr/local/share/ca-certificates
JAVA_KEY_STORE=/usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts
declare -a certs=($GITLAB_URL $NEXUS_BASE_URL)
## now loop through the above array
for url in "${certs[@]}"
do
  echo "[INFO] Checking URL = $url"

  if [[ $url =~ (https?:\/\/)([^:^\/]*)(:[0-9]+)?(\/.*)? ]]; then
    PROTOCOL=${BASH_REMATCH[1]}
    SERVER=${BASH_REMATCH[2]}
    PORT=${BASH_REMATCH[3]}
    if [ -z "$PORT" ]; then
      PORT="443"
    else
      PORT=${PORT:1}
    fi
      #certs if given URL is https
    if [[ $PROTOCOL == "https://" ]]; then
      echo "[INFO] Installing certificate from $SERVER:$PORT"
      echo -n | openssl s_client -connect $SERVER:$PORT | \
        sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' | \
        tee "${CA_CERTS_DIRECTORY}/${SERVER}.crt"
      echo "[INFO] Certificate downloaded at ${CA_CERTS_DIRECTORY}/${SERVER}.crt"
    else
      echo "[WARN] $url is not a https url. Certificate will not be installed."
    fi
  else
    echo "[ERROR] unable to parse $url URL"
    return -1
  fi
done
update-ca-certificates
  #Install certificate in java and server
echo "[INFO] Importing certificates in ${CA_CERTS_DIRECTORY} into Java keystore ${JAVA_KEY_STORE}..."
find ${CA_CERTS_DIRECTORY} -name *.crt -exec keytool -import -trustcacerts \
  -keystore ${JAVA_KEY_STORE} -storepass changeit -noprompt -file {} -alias {} \;

