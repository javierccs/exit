#Install certificates (if necesary) in Jenkins
#from configuration URLs

declare -a certs=($GITLAB_URL $NEXUS_BASE_URL)
## now loop through the above array
for url in "${certs[@]}"
do
   /usr/local/bin/install-cert.sh $url
done
update-ca-certificates
