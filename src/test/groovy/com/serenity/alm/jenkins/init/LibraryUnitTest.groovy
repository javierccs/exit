package com.serenity.alm.jenkins.init

import com.serenity.alm.jenkins.util.EnvironmentUtil
import com.serenity.alm.jenkins.util.GroovyScriptUtil
import org.codefirst.SimpleThemeDecorator
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import spock.lang.Specification

/**
 * Created by n64168 on 01/04/2017.
 */
class LibraryUnitTest extends Specification{
    def JENKINS_TEST_URL = 'http://localhost/jenkins'
    def GITLAB_TEST_URL = 'http://localhost/gitlab'

    def "No library provided"() {
        given:
        def globalLibraries = setupTest('', '')
        when:
        GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.LIBRARY_CONFIG_GROOVY, false)
        // object under test
        then:
        0 * globalLibraries.setLibraries(_)
    }
    def "No library provided must add a warning message"() {
        given:
        def globalLibraries = setupTest('', '')
        def result = GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.LIBRARY_CONFIG_GROOVY, false)
        // object under test
        expect:
        result.output.toString().contains ("Serenity ALM Library won't be set")
    }
    def "If library env vars are set, global library must be set" (){
        given:
        final libraryUrl = 'http://localhost/git/library.git'
        final libraryVersion = '1.0'
        def globalLibraries = setupTest(libraryUrl, libraryVersion)
        def result
        when:
        result = GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.LIBRARY_CONFIG_GROOVY)
        then:
        1 * globalLibraries.setLibraries(_) >> { args ->
            List<LibraryConfiguration> libraries = args[0]
            assert libraries.size() == 1
            LibraryConfiguration lc = libraries.get(0)
            assert lc.name == 'alm'
            assert lc.retriever.scm.id == 'serenity-alm-library'
            assert lc.retriever.scm.remote == "$libraryUrl"
            assert lc.implicit == true
            assert lc.allowVersionOverride == false
            assert lc.defaultVersion == libraryVersion
        }
    }
    def "If only library versions is set, global library default url must be set" (){
        given:
        final libraryVersion = '1.0'
        def globalLibraries = setupTest('', libraryVersion)
        def result
        when:
        result = GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.LIBRARY_CONFIG_GROOVY)
        then:
        1 * globalLibraries.setLibraries(_) >> { args ->
            List<LibraryConfiguration> libraries = args[0]
            assert libraries.size() == 1
            LibraryConfiguration lc = libraries.get(0)
            assert lc.retriever.scm.remote == "$GITLAB_TEST_URL/serenity-alm/serenity-alm-jenkins-library.git"
        }

    }
    def "If only library versions is set, global library default url must be set (gitlab url ends with /" (){
        given:
        final libraryVersion = '1.0'
        def globalLibraries = setupTest('', libraryVersion, GITLAB_TEST_URL + '/')
        def result
        when:
        result = GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.LIBRARY_CONFIG_GROOVY)
        then:
        1 * globalLibraries.setLibraries(_) >> { args ->
            List<LibraryConfiguration> libraries = args[0]
            assert libraries.size() == 1
            LibraryConfiguration lc = libraries.get(0)
            assert lc.retriever.scm.remote == "$GITLAB_TEST_URL/serenity-alm/serenity-alm-jenkins-library.git"
        }

    }
    private setupTest(String libraryRepository, String libraryVersion, String gitlabUrl = GITLAB_TEST_URL) {
        GroovyMock(System, global: true)
        GroovyMock (jenkins.model.Jenkins, global: true, useObjenesis: true)
        def jenkins = Mock(jenkins.model.Jenkins)
        GlobalLibraries globalLibraries = Mock (GlobalLibraries)
        jenkins.getDescriptor(_) >> globalLibraries
        jenkins.model.Jenkins.getInstance() >> jenkins
        def env = [:]
        env[EnvironmentUtil.JENKINS_URL] = JENKINS_TEST_URL
        env[EnvironmentUtil.GITLAB_URL] = gitlabUrl
        env[EnvironmentUtil.SERENITY_ALM_LIBRARY_REPOSITORY] = libraryRepository
        env[EnvironmentUtil.SERENITY_ALM_LIBRARY_VERSION] = libraryVersion
        System.getenv(_) >> { args ->
            return env[args[0]]?:''
        }
        return globalLibraries
    }
}
