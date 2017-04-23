FROM jenkins:2.46.1
MAINTAINER serenity-alm <serenity-alm@isban.com>

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY

ENV com.serenity.imageowner="Serenity-ALM" \
    com.serenity.description="Jenkins" \
    com.serenity.components="git;zip" \
    com.serenity.image.version="latest" \
    SERENITYALM_CSS=css/serenity-alm/serenity-alm.css \
    SERENITYALM_JS=scripts/serenity-alm/serenity-alm.js \
    SERENITYALM_PORTAL=http://serenity.gs.corp/web/alm \
    GIT_SSL_NO_VERIFY=1 \
    JAVA_OPTS="-Dhudson.model.ParametersAction.keepUndefinedParameters=true -Djenkins.install.runSetupWizard=false" \
    CURL_CONNECTION_TIMEOUT=10

# Install plugins
COPY config/plugins.txt /usr/share/jenkins/plugins.txt
RUN export http_proxy=$HTTP_PROXY; export https_proxy=$HTTPS_PROXY; export no_proxy=$NO_PROXY \
    && /usr/local/bin/install-plugins.sh $(cat /usr/share/jenkins/plugins.txt | tr '\n' ' ')
#Copies static config files

USER root

#Downloads and installs td-agent
#changes td-agent default user to jenkins
#and changes td-agent directories to jenkins user
RUN export http_proxy=$HTTP_PROXY; export https_proxy=$HTTPS_PROXY; export no_proxy=$NO_PROXY \
 &&  curl https://packages.treasuredata.com/GPG-KEY-td-agent | apt-key add - \
 && echo "deb http://packages.treasuredata.com/2/debian/jessie/ jessie contrib" > /etc/apt/sources.list.d/treasure-data.list \
 && apt-get update \
 && apt-get install -y --force-yes td-agent gettext-base zip \
 && sed -i 's/TD_AGENT_USER=td-agent/TD_AGENT_USER=jenkins/g' /etc/init.d/td-agent \
 && sed -i 's/TD_AGENT_GROUP=td-agent/TD_AGENT_GROUP=jenkins/g' /etc/init.d/td-agent \
 && chown -R jenkins:jenkins /etc/td-agent /opt/td-agent /var/log/td-agent /var/run/td-agent \
 && rm -rf /var/lib/apt
#Copies td-agent configuration file
COPY td-agent/td-agent.conf /etc/td-agent/td-agent.conf

#Jenkins entry point has been modified to add td-agent service
#To start td-agent service SERENITY_FLUENTD_SERVER variable must set
ADD scripts /opt/serenity-alm/scripts
RUN chown jenkins:jenkins  /opt/serenity-alm/scripts/*.sh && cp /opt/serenity-alm/scripts/*.sh /usr/local/bin

COPY theme /opt/theme
COPY config/ /usr/share/jenkins/ref/

ENTRYPOINT [ "/usr/local/bin/jenkins-entry-point.sh" ]
