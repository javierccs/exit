FROM jenkins:1.625.2
MAINTAINER serenity-alm <noreply@serenity-alm.corp>

COPY plugins.txt /usr/share/jenkins/ref/
# Modify built-in plugins.sh script, in order to add proxy to curl
COPY plugins.sh /usr/local/bin/plugins.sh

USER root
RUN chmod +x /usr/local/bin/plugins.sh
USER jenkins

# Install plugins
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt
COPY plugins/ /usr/share/jenkins/ref/plugins/
RUN for f in /usr/share/jenkins/ref/plugins/*; do unzip -qqt $f; done

# Static config files
COPY config/ /usr/share/jenkins/ref/
