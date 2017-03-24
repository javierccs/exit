FROM jenkins:2.32.3-alpine
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
    JAVA_OPTS="-Dhudson.model.ParametersAction.keepUndefinedParameters=true -Djenkins.install.runSetupWizard=false"

# Install plugins
COPY config/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN export http_proxy=$HTTP_PROXY; export https_proxy=$HTTPS_PROXY; export no_proxy=$NO_PROXY \
    && /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt
#Copies static config files

USER root

RUN export http_proxy=$HTTP_PROXY; export https_proxy=$HTTPS_PROXY; export no_proxy=$NO_PROXY \
    && apk add --update --no-cache gettext openssl zip
ADD scripts /opt/serenity-alm/scripts
RUN chown jenkins:jenkins  /opt/serenity-alm/scripts/*.sh && cp /opt/serenity-alm/scripts/*.sh /usr/local/bin

COPY theme /opt/theme
COPY config/ /usr/share/jenkins/ref/

ENTRYPOINT [ "/usr/local/bin/jenkins-entry-point.sh" ]

