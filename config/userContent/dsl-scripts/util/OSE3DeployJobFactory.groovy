package util
import util.AuthorizationJobFactory

class OSE3DeployJobFactory{

  static getHideJobName (String gitlabProjectName){
    return gitlabProjectName + '-ose3-pro-deploy-shadow'
  }

  static getProJobName (boolean blueGreenDeployment, String gitlabProjectName){
    return gitlabProjectName + (blueGreenDeployment?'-ose3-pro-route-switch':'-ose3-pro-deploy')
  }

  static getDeployProCheckJobName (gitlabProjectName){
    return gitlabProjectName + '-ose3-pro-check-deploy'
  }

  // Generates production approval and deployment jobs
  // if blue green deployment is set then deploy to shadow and switch deployment jobs are created
  // else deploy to production deployment job is created
  /**
   * @param dslFactory Job DSL Factory (usually this)
   * @param blueGreenDeployment true if blue green deployment is desired
   * @param buildName Buildname to set
   * @param jobArgs Params to pass to deployment job
   * @param gitlabProjectName Repository name. Is used to generate deployment job names
   * @param ose3Url OpenShift url
   * @param ose3Project Openshift project name (with -pro)
   * @param ose3Application Openshift application name
   * @param ose3Template deployment tempalte name
   * @ose3TemplateParams template params
   * @nodes nodes to add to deployment job
   */
  static createOse3ProJobs (dslFactory, blueGreenDeployment, String buildName,
      jobArgs, gitlabProjectName,
      ose3Url, ose3Project, ose3Application, ose3Template, ose3TemplateParams,
      nodes = []
      ) {
    def utils = dslFactory.evaluate(
      new File("${dslFactory.JENKINS_HOME}/userContent/dsl-scripts/util/Utils.groovy"))
    // creates production approval job
    AuthorizationJobFactory.createApprovalJob(dslFactory,
      OSE3DeployJobFactory.getDeployProCheckJobName(gitlabProjectName), true,
      buildName, jobArgs,
      blueGreenDeployment?getHideJobName(gitlabProjectName):getProJobName(false, gitlabProjectName),
      blueGreenDeployment)

    //Deploy in hide environment job
    def jobName = blueGreenDeployment?getHideJobName(gitlabProjectName):getProJobName(false, gitlabProjectName)
    dslFactory.job (jobName) {
      dslFactory.out.println "OSE3DeployJobFactory: $jobName"
      using('TJ-ose3-deploy')
      disabled(false)
if (blueGreenDeployment) {
   // blue green deployment
      deliveryPipelineConfiguration('Shadow', 'Deploy to shadow')
}
else {
  // no blue green deployment
      deliveryPipelineConfiguration('PRO', 'Deploy to production')
} // end blueGreenDeployment
      parameters {
        jobArgs.each {
          stringParam(it[0], it[1], it[2]);
        }
      }
      properties {
// includes promote to pro
if (blueGreenDeployment) {
        promotions {
          promotion {
            name('Promote-PRO')
            icon('star-gold-e')
            conditions {
              manual(Utilities.getProPromotionRoleGroups())
            }
            actions {
              downstreamParameterized {
                trigger(getProJobName(true, gitlabProjectName)) {
                  parameters {
                    jobArgs.each {
                        predefinedProp(it[0], '${' + it[0] + '}');

                    } //end jobargs
                    predefinedProp('OSE3_TOKEN_PROJECT', '${OSE3_TOKEN_PROJECT}')
                  }
                }
              }
            }
          }
        }
} // end blue green deployment promotion
      }
      configure {
        utils.removeParam(it, 'OSE3_TEMPLATE_PARAMS')
        utils.updateParam(it, 'OSE3_URL', ose3Url)
        utils.updateParam(it, 'OSE3_PROJECT_NAME', ose3Project)
        utils.updateParam(it, 'OSE3_APP_NAME',  ose3Application)
        utils.updateParam(it, 'OSE3_TEMPLATE_NAME', ose3Template)
        utils.updateParam(it, 'OSE3_TOKEN_PROJECT', '')
        utils.updateParam(it, 'OSE3_BLUE_GREEN', blueGreenDeployment?'ON':'OFF')
// adds optional xml nodes
for (def nodeXml : nodes ) {
        (it / builders).children().add(0, new XmlParser().parseText(nodeXml))
} // end add nodes
      }
    }
// this job is generated only if blue green deployment is set
// this jobs executes switch between blue and green
if (blueGreenDeployment) {
    //Deploy in pro job
    def finalProJobName = getProJobName(true, gitlabProjectName)
    dslFactory.job (finalProJobName) {
      dslFactory.out.println "JOB: $finalProJobName"
      using('TJ-ose3-switch')
      disabled(false)
      deliveryPipelineConfiguration('PRO', 'Switch from Shadow to PRO')
      parameters {
        jobArgs.each {
          stringParam(it[0], it[1], it[2]);
        }
      }

      configure {
        utils.updateParam(it, 'OSE3_URL', ose3Url)
        utils.updateParam(it, 'OSE3_PROJECT_NAME', ose3Project )
        utils.updateParam(it, 'OSE3_APP_NAME',  ose3Application)
        utils.updateParam(it, 'OSE3_TOKEN_PROJECT', '')

  // adds optional xml nodes
  for (def nodeXml : nodes ) {
        (it / builders).children().add(0, new XmlParser().parseText(nodeXml))
  } // end add nodes

      }
    }
} // end blue green deployment
  }
}
