FROM jenkins:2.7.4
MAINTAINER serenity-alm <serenity-alm@isban.com>

ENV com.serenity.imageowner="Serenity-ALM" \
    com.serenity.description="Jenkins" \
    com.serenity.components="git;zip" \
    com.serenity.image.version="1.4.1"

ENV SERENITYALM_CSS=css/serenity-alm/serenity-alm.css \
    SERENITYALM_JS=scripts/serenity-alm/serenity-alm.js \
    SERENITYALM_PORTAL=http://portalserenity.eng.gsnetcloud.corp:8080/web/alm \
    GIT_SSL_NO_VERIFY=1 \
    JAVA_OPTS="-Dhudson.model.ParametersAction.keepUndefinedParameters=true -Djenkins.install.runSetupWizard=false"

USER root

#Downloads td-agent (sets proxy for download)
ENV http_proxy=http://proxyapps.gsnet.corp:80 \
    https_proxy=http://proxyapps.gsnet.corp:80 \
    no_proxy="*.gsnet.corp, *.gsnetcloud.corp"

#Downloads and installs td-agent
#changes td-agent default user to jenkins
#and changes td-agent directories to jenkins user
RUN  curl https://packages.treasuredata.com/GPG-KEY-td-agent | apt-key add - \
 && echo "deb http://packages.treasuredata.com/2/debian/jessie/ jessie contrib" > /etc/apt/sources.list.d/treasure-data.list \
 && apt-get update \
 && apt-get install -y --force-yes td-agent gettext-base \
 && sed -i 's/TD_AGENT_USER=td-agent/TD_AGENT_USER=jenkins/g' /etc/init.d/td-agent \
 && sed -i 's/TD_AGENT_GROUP=td-agent/TD_AGENT_GROUP=jenkins/g' /etc/init.d/td-agent \
 && chown -R jenkins:jenkins /etc/td-agent \
 && chown -R jenkins:jenkins /opt/td-agent \
 && chown -R jenkins:jenkins /var/log/td-agent \ 
 && chown -R jenkins:jenkins /var/run/td-agent  
#Copies td-agent configuration file
COPY td-agent/td-agent.conf /etc/td-agent/td-agent.conf
#Unset proxy
ENV http_proxy="" \
    https_proxy="" \
    no_proxy=""

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

#Jenkins entry point has been modified to add td-agent service
#To start td-agent service SERENITY_FLUENTD_SERVER variable must set
USER root
ADD scripts /opt/serenity-alm/scripts
RUN chown jenkins:jenkins  /opt/serenity-alm/scripts/*.sh && cp /opt/serenity-alm/scripts/*.sh /usr/local/bin

#Jenkins war will be modified by entrypoint to add serenity-alm.css
RUN chown jenkins:jenkins /usr/share/jenkins/jenkins.war
COPY theme /opt/theme

ENTRYPOINT [ "/usr/local/bin/jenkins-entry-point.sh" ]
