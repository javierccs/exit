FROM jenkins:2.46.2-alpine
MAINTAINER alm.uk <ALM.UK@produban.co.uk>

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG NO_PROXY

ENV com.isbanuk.imageowner="alm.uk" \
    com.isbanuk.description="Jenkins" \
    com.isbanuk.components="git;zip" \
    com.isbanuk.image.version="latest" \
    GIT_SSL_NO_VERIFY=1 \
    JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Install plugins
COPY config/plugins.txt /usr/share/jenkins/plugins.txt
RUN export http_proxy=$HTTP_PROXY; export https_proxy=$HTTPS_PROXY; export no_proxy=$NO_PROXY \
    && /usr/local/bin/install-plugins.sh $(cat /usr/share/jenkins/plugins.txt | tr '\n' ' ')

#Copies static config files
USER root
RUN export http_proxy=$HTTP_PROXY; export https_proxy=$HTTPS_PROXY; export no_proxy=$NO_PROXY \
    && apk add --update --no-cache gettext openssl zip
ADD scripts /opt/alm-uk/scripts
RUN chown jenkins:jenkins  /opt/alm-uk/scripts/*.sh && cp /opt/alm-uk/scripts/*.sh /usr/local/bin
COPY config /usr/share/jenkins/ref/


ENTRYPOINT [ "/usr/local/bin/jenkins-entry-point.sh" ]
