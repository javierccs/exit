package com.serenity.alm.jenkins.init

import com.serenity.alm.jenkins.util.EnvironmentUtil
import com.serenity.alm.jenkins.util.GroovyScriptUtil
import spock.lang.Specification
import jenkins.model.Jenkins
import org.codefirst.SimpleThemeDecorator

import java.lang.reflect.Field

/**
 * Created by n64168 on 31/03/2017.
 * Unit Tests for simple-theme.groovy init script
 */
class SimpleThemeUnitTest extends Specification{
    def JENKINS_TEST_URL = 'http://localhost/jenkins'


    // run before every feature method
    def setup() {
    }

    def "No configuration provided"() {
        given:
        def themeDecorator = setupTest('', '')
        when:
        GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.SIMPLE_THEME_GROOVY)
        // object under test
        then:
        0 * themeDecorator.cssUrl(_)
        0 * themeDecorator.jsUrl(_)
    }
    /**
     * can't mock SimpleThemeDecorator.setxx because these are private members
    def "style provided"() {
        given:
        def cssUri = 'css.css'
        def jsUri = 'jsUri.js'
        def themeDecorator = setupTest(cssUri, jsUri)
        when:
        GroovyScriptUtil.executeGroovyFile(InitGroovyUtil.SIMPLE_THEME_GROOVY)
        // object under test
        then:
        1 * themeDecorator.cssUrl("$JENKINS_TEST_URL/$cssUri")
        1 * themeDecorator.jsUrl("$JENKINS_TEST_URL/$jsUri")
        1 * themeDecorator.save()
    }
    **/
    private setupTest(String cssUri, String jsUri){
        GroovyMock(System, global: true)
        GroovyMock (Jenkins, global: true, useObjenesis: true)
        def jenkins = Mock(Jenkins)
        SimpleThemeDecorator decorator = Mock (cssUrl: '', jsUrl: '')

        jenkins.getDescriptor(_) >> decorator
        Jenkins.getInstance() >> jenkins
        def env = [:]
        env[EnvironmentUtil.JENKINS_URL] = JENKINS_TEST_URL
        env[EnvironmentUtil.SERENITYALM_CSS] = cssUri
        env[EnvironmentUtil.SERENITYALM_JS] = jsUri
        System.getenv(_) >> { args ->
            return env[args[0]]?:''
        }
        return decorator
    }
}
