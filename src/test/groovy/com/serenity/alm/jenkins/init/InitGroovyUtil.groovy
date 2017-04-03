package com.serenity.alm.jenkins.init

/**
 * Created by n64168 on 31/03/2017.
 */
class InitGroovyUtil {
    static final INIT_GROOVY_DIRECTORY = 'config/init.groovy.d'
    //simple theme plugin
    static final String SIMPLE_THEME_GROOVY = "$INIT_GROOVY_DIRECTORY/simple-theme.groovy"
    static final String SIMPLE_THEME_DECORATOR = 'org.codefirst.SimpleThemeDecorator'

    // mailer plugin
    static final String MAIL_CONFIG_GROOVY = "$INIT_GROOVY_DIRECTORY/mail-config.groovy"
    static final String MAILER_PLUGIN = 'hudson.tasks.Mailer'

    // library plugin
    static final String LIBRARY_CONFIG_GROOVY = "$INIT_GROOVY_DIRECTORY/library.groovy"
    static final String GLOBAL_LIBRARIES_PLUGIN = 'org.jenkinsci.plugins.workflow.libs.GlobalLibraries'

}
