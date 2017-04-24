FROM jenkins:2.46.1-alpine
MAINTAINER serenity-alm <serenity-alm@isban.com>

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY
ARG ALM_FLAVOUR=hyperloop

ENV com.serenity.imageowner="Serenity-ALM" \
    com.serenity.description="Jenkins" \
    com.serenity.components="git;zip" \
    com.serenity.image.version="latest" \
    ALM_FLAVOUR=$ALM_FLAVOUR \
    GIT_SSL_NO_VERIFY=1 \
    JAVA_OPTS="-Dhudson.model.ParametersAction.keepUndefinedParameters=true -Djenkins.install.runSetupWizard=false"

# Install plugins
COPY config/common/plugins.txt /usr/share/jenkins/plugins.txt
RUN export http_proxy=$HTTP_PROXY; export https_proxy=$HTTPS_PROXY; export no_proxy=$NO_PROXY \
    && /usr/local/bin/install-plugins.sh $(cat /usr/share/jenkins/plugins.txt | tr '\n' ' ')
#Copies static config files

USER root

RUN export http_proxy=$HTTP_PROXY; export https_proxy=$HTTPS_PROXY; export no_proxy=$NO_PROXY \
    && apk add --update --no-cache gettext openssl zip
ADD scripts /opt/serenity-alm/scripts
RUN chown jenkins:jenkins  /opt/serenity-alm/scripts/*.sh && cp /opt/serenity-alm/scripts/*.sh /usr/local/bin

COPY theme/$ALM_FLAVOUR /opt/theme
COPY config/common /usr/share/jenkins/ref/
COPY config/$ALM_FLAVOUR /usr/share/jenkins/ref/

ENTRYPOINT [ "/usr/local/bin/jenkins-entry-point.sh" ]
