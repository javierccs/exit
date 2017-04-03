package com.serenity.alm.jenkins.main.test;
import org.jvnet.hudson.test.JenkinsRule;
import org.apache.commons.io.FileUtils;
import hudson.model.*;
import hudson.tasks.Shell;
import org.junit.Test;
import org.junit.Rule;
public class AppIntegrationTest {
  static{
    System.out.println ("start...");
  }
  @Rule public JenkinsRule j = new JenkinsRule();
  static{
    System.out.println ("end rule");
  }
  @Test public void first() throws Exception {
    System.out.println ("first 1:");
    FreeStyleProject project = j.createFreeStyleProject();
    project.getBuildersList().add(new Shell("echo hello"));
    FreeStyleBuild build = project.scheduleBuild2(0).get();
    System.out.println(build.getDisplayName() + " completed");
    // TODO: change this to use HtmlUnit
    String s = FileUtils.readFileToString(build.getLogFile());
    
  //  assertThat(s, s.contains("+ echo hello"));
  }
}
