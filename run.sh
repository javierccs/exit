#!/bin/bash

git pull origin feature_273_JOB_Template_OS3_Project

docker stop $(docker ps -q -f name=jenkins)

docker rm $(docker ps -aq -f name=jenkins)

docker build -t ics .

docker run --name=jenkins -d -v /root/jenkins/data \
         -e SMTP_HOST=mailintra.gsnet.corp \
         -e SMTP_PORT=25 \
         -e MAVEN_DEPLOYER_LOGIN=deployment \
         -e MAVEN_DEPLOYER_PASSWD=deployment123 \
         -e NEXUS_BASE_URL="https://nexus.ci.gsnet.corp/nexus" \
         -e JENKINS_PASSWORD=admin1 \
         -e JENKINS_EMAIL=xxxxx -e JENKINS_URL="http://oclubunc017.isbcloud.isban.corp:8081" \
         -e GITLAB_URL=http://srnalmgpro303.eng.gsnetcloud.corp \
         -e GITLAB_API_TOKEN=JHnZMionVSMUexsAW19S \
         -e SWARM_MASTER_URL="http://oclubunc017.isbcloud.isban.corp:2375" \
         -e LDAP_SERVER=ldap.produban.pre.corp \
         -e LDAP_PASSWORD=Caracol2015 \
         -e LDAP_BIND_DN="uid=admtc,ou=People,o=Produban,c=es,o=Grupo Santander" \
         -e LDAP_BASE="ou=users,ou=lvtc,cn=BanksphereIntranet,o=Produban,c=es,o=Grupo Santander" \
         -e LDAP_GROUP_BASE="ou=groups,ou=lvtc,cn=BanksphereIntranet,o=Produban,c=es,o=Grupo Santander" \
         -e LDAP_GROUP_FILTER="(&(cn={0})(objectClass=groupOfNames))" \
         -e LDAP_GROUPS=alm-technical-lead,alm-developer,alm-product-owner \
          -p 8081:8080 -p 50000:50000 -e HOSTNAME=$(hostname) \
          -e SERENITY_FLUENTD_SERVER=180.46.38.195 -e SERENITY_FLUENTD_PORT=24224 -e SERENITY_TENANT=ics ics