#Install certificates (if necesary) in Jenkins
#from configuration URLs

declare -a certs=($GITLAB_URL $NEXUS_BASE_URL)
## now loop through the above array
for url in "${certs[@]}"
do
   /usr/local/bin/install-cert.sh $url
done
update-ca-certificates
CA_CERTS=/etc/ssl/certs/ca-certificates.crt
if [[ $GITLAB_URL =~ (https?:\/\/)([^:^\/]*)(:[0-9]+)?(\/.*)? ]]; then
    PROTOCOL=${BASH_REMATCH[1]}
    SERVER=${BASH_REMATCH[2]}
    PORT=${BASH_REMATCH[3]}
    if [ -z "$PORT" ]; then
      PORT="443"
    else
      PORT=${PORT:1}
    fi
    if [[ $PROTOCOL == "https://" ]]; then
      echo "Adding GitLab certificate to $CA_CERTS"
      echo $(echo -n | openssl s_client -showcerts -connect $SERVER:$PORT 2>/dev/null  | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p') >> $CA_CERTS
      echo "GitLab certificate added to $CA_CERTS"
    fi
else
  echo "[ERROR] unable to parse $1 URL"
  return -1
fi
