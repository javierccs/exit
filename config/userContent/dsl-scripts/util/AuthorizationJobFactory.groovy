package util
class AuthorizationJobFactory{
  // creates a job for approval
  // @param dslFactory dslFactory instance
  // @param jobName job to create
  // @param production Boolean true for pro false for pre
  // @param jobBuildName Build name
  // @jobStringParams Job String parameters
  // @param triggeredJobName Job to trigger
  // @param blueGreenDeployment B
  // @param triggeredJobTokenParam triggered job's ose3 token param name ('OSE3_TOKEN_PROJECT' by default)
  static createApprovalJob (dslFactory, jobName, production,
    jobBuildName, jobStringParams, triggeredJobName, blueGreenDeployment = false,
    triggeredJobTokenParam = 'OSE3_TOKEN_PROJECT') {
    // pre approval job
    dslFactory.out.println ("createApprovalJob: " + jobName)
    dslFactory.job (jobName) {
      disabled(false)
      deliveryPipelineConfiguration((production ? 'PRO Approval' : 'PRE Approval'), 'Manual approval')
      wrappers {
        buildName(jobBuildName)
      }
      parameters {
        jobStringParams.each {
          stringParam(it[0], it[1], it[2]);
        }
      }
      properties {
        promotions {
          promotion {
            name(production ? (blueGreenDeployment?'Promote-Shadow':'Promote-PRO') : 'Promote-PRE')
            icon(production ? 'star-gold-w' : 'star-silver-w')
            conditions {
              manual(production ? Utilities.getProPromotionRoleGroups() : Utilities.getPrePromotionRoleGroups()) {
                parameters {
                  credentialsParam('OSE3_DEPLOYMENT_TOKEN_CREDENTIAL'){
                    type('org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl')
                    required()
                    defaultValue(null)
                    description('OpenShift deployment Token')
                  }
                }
              }
            }
            actions {
              // Executes a system Groovy script.
              systemGroovyCommand(AuthorizationJobFactory.getAuthorizationGroovy()) {
                // Adds a variable binding for the script.
                binding('CRED_PARAM_NAME', 'OSE3_DEPLOYMENT_TOKEN_CREDENTIAL')
                binding('SECRET_NAME', 'OSE3_DEPLOYMENT_TOKEN_VALUE')
              }
              downstreamParameterized {
                trigger(triggeredJobName) {
                  parameters {
                    jobStringParams.each {
                      predefinedProp(it[0], '${' + it[0] + '}');
                    }
                    predefinedProp('OSE3_TOKEN_PROJECT', '$OSE3_DEPLOYMENT_TOKEN_VALUE')
                  }
                }
              }
            }
          }
        }
      }
    }
  } //end createApprovalJob
  static getAuthorizationGroovy() {
  return """
import com.cloudbees.plugins.credentials.common.StandardCredentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import hudson.model.*

// Get Promotion actions needed
def params = build.getAction(hudson.plugins.promoted_builds.Promotion.PromotionParametersAction)
def target = build.getAction(hudson.plugins.promoted_builds.PromotionTargetAction)
def cause = build.getCause(Cause.UserCause)
assert (!(params == null || target == null || cause == null)) : "[ERROR] This is not a promotion build"

// Get the user credential
def credId = params.find { it.name == CRED_PARAM_NAME }.value
def item = jenkins.model.Jenkins.instance
def auth = User.get(cause.getUserName(), false, Collections.emptyMap()).impersonate()
def cred = CredentialsProvider.lookupCredentials(StandardCredentials.class,item,auth,null).find { credId == it.getId() }

// Add a new PasswordParameterValue to Promotion Actions with the secret plain text
def mod_parameters = new ArrayList<StringParameterValue>();
mod_parameters.add(new PasswordParameterValue(SECRET_NAME, cred.getSecret().getPlainText()))
mod_parameters.addAll(params.getParameters())
def promoted_params = hudson.plugins.promoted_builds.Promotion.PromotionParametersAction.buildFor(target.resolve(), mod_parameters)
build.actions.remove(params)
build.actions.add(promoted_params)"""
  }
} // AuthorizationJobFactory
