package com.serenity.alm.jenkins.init

import com.serenity.alm.jenkins.util.EnvironmentUtil
import com.serenity.alm.jenkins.util.GroovyScriptUtil
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Specification

/**
 * Created by n64168 on 01/04/2017.
 */
class LibraryIntegrationTest extends Specification{
    def JENKINS_TEST_URL = 'http://localhost/jenkins'
    def GITLAB_TEST_URL = 'http://localhost/gitlab'

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    @Rule
    JenkinsRule j = new JenkinsRule()

    def "If library env vars are set, global library must be set" (){
        given:
        final libraryUrl = 'http://localhost/git/library.git'
        final libraryVersion = '1.0'
        GlobalLibraries globalLibraries
        setupEnvironmentVars(libraryUrl, libraryVersion)
        println ("JENKINS_URL = ${System.getenv('JENKINS_URL')}")
        GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.LIBRARY_CONFIG_GROOVY)
        globalLibraries = j.jenkins.getDescriptor(InitGroovyUtil.GLOBAL_LIBRARIES_PLUGIN)
        LibraryConfiguration lc = globalLibraries.libraries.get(0)
        expect:
        globalLibraries.libraries.size() == 1
        lc.name == 'alm'
        lc.retriever.scm.id == 'serenity-alm-library'
        lc.retriever.scm.remote == "$libraryUrl"
        lc.implicit == true
        lc.allowVersionOverride == false
        lc.defaultVersion == libraryVersion
    }
    private setupEnvironmentVars(String libraryRepository, String libraryVersion, String gitlabUrl = GITLAB_TEST_URL) {
        environmentVariables.set(EnvironmentUtil.JENKINS_URL, JENKINS_TEST_URL)
        environmentVariables.set(EnvironmentUtil.GITLAB_URL, gitlabUrl)
        environmentVariables.set(EnvironmentUtil.SERENITY_ALM_LIBRARY_REPOSITORY, libraryRepository)
        environmentVariables.set(EnvironmentUtil.SERENITY_ALM_LIBRARY_VERSION, libraryVersion)
    }
}
