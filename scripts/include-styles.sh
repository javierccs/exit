#!/bin/bash
#Includes styles in jenkins war
#Adds image version to css
THEME_TMP_DIR=/tmp/theme
mkdir $THEME_TMP_DIR
export com_serenity_image_version=$(env | grep version | cut -d '=' -f 2)
echo "[INFO] Including ALM v. $com_serenity_image_version style in Jenkins..."
cp -R /opt/theme/* $THEME_TMP_DIR
if [ -f /tmp/theme/$SERENITYALM_CSS.template  ];
then
  envsubst < /tmp/theme/$SERENITYALM_CSS.template > /tmp/theme/$SERENITYALM_CSS
  # template is deleted
  rm $THEME_TMP_DIR/$SERENITYALM_CSS.template
fi
if [ -f /tmp/theme/$SERENITYALM_JS.template  ];
then
  envsubst < /tmp/theme/$SERENITYALM_JS.template > /tmp/theme/$SERENITYALM_JS
  # template is deleted
  rm $THEME_TMP_DIR/$SERENITYALM_JS.template
fi
cp /usr/share/jenkins/jenkins.war /tmp/jenkins.war
current=$PWD
cd $THEME_TMP_DIR
zip -ur  /tmp/jenkins.war css images scripts
cp /tmp/jenkins.war  /usr/share/jenkins/jenkins.war
cd $current
rm -rf $THEME_TMP_DIR
echo "[INFO] ALM $SERENITYALM_CSS style added"
