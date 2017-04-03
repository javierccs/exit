package com.serenity.alm.jenkins.main

import hudson.model.FreeStyleBuild
import hudson.model.FreeStyleProject
import hudson.tasks.Shell
import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Specification

/**
 * Test if jenkins is started successfully
 * And it has everything configurated properly
 */
class AppIntegrationTest extends Specification {
    @Rule def JenkinsRule j = new JenkinsRule()
    def "jenkins must say hello"(){
        given: "A jenkins instance to test."
        def test = 'echo "Hello world"'
        System.out.println ("first 1:");
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(new Shell("echo hello"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        expect: "Job must be executed successfully"
        s.contains("+ echo hello")

    }
}