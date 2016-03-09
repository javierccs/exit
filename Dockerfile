FROM jenkins:1.625.2
MAINTAINER serenity-alm <noreply@serenity-alm.corp>

LABEL description="Serenity ALM Jenkins image"
LABEL com.serenity.imageowner="Serenity-ALM" \
      com.serenity.description="Jenkins" \
      com.serenity.components="git;zip" \
      com.serenity.image.version="1.0-SNAPSHOT"

ENV com.serenity.imageowner="Serenity-ALM" \
    com.serenity.description="Jenkins" \
    com.serenity.components="git;zip" \
    com.serenity.image.version="1.0-SNAPSHOT"
	
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

#Installs jenkins plugins and
COPY plugins.txt /usr/share/jenkins/ref/
# Modify built-in plugins.sh script, in order to add proxy to curl
COPY plugins.sh /usr/local/bin/plugins.sh
RUN chmod +x /usr/local/bin/plugins.sh

USER jenkins

# Installis plugins
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt
COPY plugins/ /usr/share/jenkins/ref/plugins/
RUN for f in /usr/share/jenkins/ref/plugins/*; do unzip -qqt $f; done


#Copies td-agent configuration file
COPY td-agent/td-agent.conf /etc/td-agent/td-agent.conf

USER root
#Jenkins entry point has been modified to add td-agent service
#To start td-agent service SERENITY_FLUENTD_SERVER variable must set
COPY td-agent/jenkins-td-agent-entry-point.sh /usr/local/bin/jenkins-td-agent-entry-point.sh

User jenkins
#Copies static config files
COPY config/ /usr/share/jenkins/ref/
ENTRYPOINT [ "/usr/local/bin/jenkins-td-agent-entry-point.sh" ]
