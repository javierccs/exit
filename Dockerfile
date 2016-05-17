FROM jenkins:1.642.4
MAINTAINER serenity-alm <noreply@serenity-alm.corp>

LABEL description="Serenity ALM Jenkins image"
LABEL com.serenity.imageowner="Serenity-ALM" \
      com.serenity.description="Jenkins" \
      com.serenity.components="git;zip" \
      com.serenity.image.version="latest"

ENV com.serenity.imageowner="Serenity-ALM" \
    com.serenity.description="Jenkins" \
    com.serenity.components="git;zip" \
    com.serenity.image.version="latest"
	
USER root

#Installs td-agent (fluentd) for log collection

#Downloads td-agent (sets proxy for download)
ENV http_proxy http://proxyapps.gsnet.corp:80
ENV https_proxy http://proxyapps.gsnet.corp:80
ENV no_proxy="*.gsnet.corp, *.gsnetcloud.corp"
#Downloads and installs td-agent
#changes td-agent default user to jenkins
#and changes td-agent directories to jenkins user
RUN  curl https://packages.treasuredata.com/GPG-KEY-td-agent | apt-key add - \
 && echo "deb http://packages.treasuredata.com/2/debian/jessie/ jessie contrib" > /etc/apt/sources.list.d/treasure-data.list \
 && apt-get update \
 && apt-get install -y --force-yes td-agent \
 && sed -i 's/TD_AGENT_USER=td-agent/TD_AGENT_USER=jenkins/g' /etc/init.d/td-agent \
 && sed -i 's/TD_AGENT_GROUP=td-agent/TD_AGENT_GROUP=jenkins/g' /etc/init.d/td-agent \
 && chown -R jenkins:jenkins /etc/td-agent \
 && chown -R jenkins:jenkins /opt/td-agent \
 && chown -R jenkins:jenkins /var/log/td-agent \ 
 && chown -R jenkins:jenkins /var/run/td-agent  
#Copies td-agent configuration file
COPY td-agent/td-agent.conf /etc/td-agent/td-agent.conf
#Jenkins entry point has been modified to add td-agent service
#To start td-agent service SERENITY_FLUENTD_SERVER variable must set
COPY td-agent/jenkins-td-agent-entry-point.sh /usr/local/bin/jenkins-td-agent-entry-point.sh
#Unset proxy
ENV http_proxy ""
ENV https_proxy ""
ENV no_proxy ""

ADD certs/nexus.ci.gsnet.corp.cer /usr/local/share/ca-certificates/
RUN update-ca-certificates && keytool -import -trustcacerts -keystore /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/cacerts   -noprompt -alias nexus.ci.gsnet.corp -file /usr/local/share/ca-certificates/nexus.ci.gsnet.corp.cer -storepass changeit

#Installs jenkins plugins and
COPY plugins.txt /usr/share/jenkins/ref/
# Modify built-in plugins.sh script, in order to add proxy to curl
COPY plugins.sh /usr/local/bin/plugins.sh
RUN chmod +x /usr/local/bin/plugins.sh

USER jenkins

# Install plugins
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt
#Copies static config files
COPY config/ /usr/share/jenkins/ref/

ENTRYPOINT [ "/usr/local/bin/jenkins-td-agent-entry-point.sh" ]
