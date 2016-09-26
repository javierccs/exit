#!/bin/bash
#Install certificate in java and server 
#certs if given URL is https
if [[ -z $1 ]]; then
  echo "[ERROR] URL is required"
fi
if [[ -z $2 ]]; then
  CA_CERTS_DIRECTORY=/usr/local/share/ca-certificates
else
  CA_CERTS_DIRECTORY=$2
fi
if [[ -z $3 ]]; then
  JAVA_KEY_STORE=/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts
else
  JAVA_KEY_STORE=$3
fi


echo "[INFO] Checking URL = $1"

if [[ $1 =~ (https?:\/\/)([^:^\/]*)(:[0-9]+)?(\/.*)? ]]; then
    PROTOCOL=${BASH_REMATCH[1]}
    SERVER=${BASH_REMATCH[2]}
    PORT=${BASH_REMATCH[3]}
    if [ -z "$PORT" ]; then
      PORT="443"
    else
      PORT=${PORT:1}
    fi
    if [[ $PROTOCOL == "https://" ]]; then
      echo "[INFO] Installing certificate from $SERVER:$PORT"
      echo -n | openssl s_client -connect $SERVER:$PORT | \
        sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' | \
        tee "${CA_CERTS_DIRECTORY}/${SERVER}.crt"
      echo "[INFO] Certificate downloaded at ${CA_CERTS_DIRECTORY}/${SERVER}.crt"
      echo "[INFO] Importing certificate into Java default keystore..."
      keytool -import -trustcacerts -keystore ${JAVA_KEY_STORE} -noprompt -alias ${SERVER} -file ${CA_CERTS_DIRECTORY}/${SERVER}.crt -storepass changeit
    else
      echo "[WARN] $1 is not a https url. Certificate will not be installed."
    fi
else
  echo "[ERROR] unable to parse $1 URL"
  return -1
fi

