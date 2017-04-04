package com.serenity.alm.jenkins.init

import com.serenity.alm.jenkins.util.EnvironmentUtil
import com.serenity.alm.jenkins.util.GroovyScriptUtil
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Specification

/**
 * Created by n64168 on 30/03/2017.
 * Integration Tests for simple-theme.groovy init script
 */
class SimpleThemeIntegrationTest extends Specification {

    final String JENKINS_TEST_URL = 'http://localhost/jenkins'

    @Rule
    JenkinsRule j = new JenkinsRule()

    // run before every feature method
    def setup() {
    }
    // run after every feature method
    def cleanup() {
    }

    def "if theme variables are not set no jenkins style must be applied"() {
        given:
        GroovyMock(System, global: true)
        def env = [:]
        env[EnvironmentUtil.JENKINS_URL] = JENKINS_TEST_URL
        System.getenv(_) >> { args ->
            return env[args[0]]?:''
        }
        GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.SIMPLE_THEME_GROOVY)
        // object under test
        def themeDecorator = j.jenkins.getDescriptor(InitGroovyUtil.SIMPLE_THEME_DECORATOR)
        expect: "No configuration must be applied"
        null == themeDecorator.cssUrl
        null == themeDecorator.jsUrl
    }

    def "configures CSS ans JS URLs"() {
        given:
        def cssUri = 'myCss.css'
        def jsUri = 'myJs.js'
        GroovyMock(System, global: true)
        def env = [:]
        env[EnvironmentUtil.JENKINS_URL] = JENKINS_TEST_URL
        env[EnvironmentUtil.SERENITYALM_CSS] =  cssUri
        env[EnvironmentUtil.SERENITYALM_JS] = jsUri
        System.getenv(_) >> { args ->
            return env[args[0]]?:''
        }
        GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.SIMPLE_THEME_GROOVY)
        def themeDecorator = j.jenkins.getDescriptor(InitGroovyUtil.SIMPLE_THEME_DECORATOR)
        expect: "Styles must be applied"
        "$JENKINS_TEST_URL/$cssUri" == themeDecorator.cssUrl
        "$JENKINS_TEST_URL/$jsUri" == themeDecorator.jsUrl
    }

    def "configures CSS ans JS URLs Jenkins URL endsWith /"() {
        given: "a jenkins with styles"
        def cssUri = 'myCss.css'
        def jsUri = 'myJs.js'
        GroovyMock(System, global: true)
        def env = [:]
        env[EnvironmentUtil.JENKINS_URL] = JENKINS_TEST_URL + '/'
        env[EnvironmentUtil.SERENITYALM_CSS] =  cssUri
        env[EnvironmentUtil.SERENITYALM_JS] = jsUri
        System.getenv(_) >> { args ->
            return env[args[0]]?:''
        }
        GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.SIMPLE_THEME_GROOVY)
        def themeDecorator = j.jenkins.getDescriptor(InitGroovyUtil.SIMPLE_THEME_DECORATOR)
        expect: "Styles must be applied"
        themeDecorator.cssUrl == "$JENKINS_TEST_URL/$cssUri"
        themeDecorator.jsUrl == "$JENKINS_TEST_URL/$jsUri"
    }
}