FROM jenkins:1.625.2
MAINTAINER Adrián Lizano <adrian.lizano@ext.produban.com>

COPY plugins.txt /usr/share/jenkins/ref/

# Modify built-in plugins.sh script, in order to add proxy to curl
COPY plugins.sh /usr/local/bin/plugins.sh

USER root
RUN chmod +x /usr/local/bin/plugins.sh

USER jenkins

# Install plugins
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt

# Static config files
COPY config/ /usr/share/jenkins/ref/

# Init groovy scripts
COPY init-scripts/ /usr/share/jenkins/ref/init.groovy.d/
