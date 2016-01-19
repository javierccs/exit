FROM jenkins:1.625.2
MAINTAINER Adrián Lizano <adrian.lizano@ext.produban.com>

COPY plugins.txt /usr/share/jenkins/ref/

# Modify built-in plugins.sh script, in order to add proxy to curl
COPY plugins.sh /usr/local/bin/plugins.sh

USER root
RUN chmod +x /usr/local/bin/plugins.sh

USER jenkins
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt

# remove executors in master
COPY master-executors.groovy /usr/share/jenkins/ref/init.groovy.d/

# Configure git
COPY git-config.groovy /usr/share/jenkins/ref/init.groovy.d/

# Configure docker cloud
COPY docker-config.groovy /usr/share/jenkins/ref/init.groovy.d/
