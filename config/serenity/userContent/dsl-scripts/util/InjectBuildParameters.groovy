/*** BEGIN META {
  "name" : "inject build parameters from build environment variables",
  "parameters" : [ENV_LIST],
  "authors" : [
    { name : "fdelamor@isban.es" }
  ]
} END META**/
import hudson.model.*

def env_vars = build.getEnvVars()

// Inject the new parameters into the existing list
def new_parameters = new ArrayList<StringParameterValue>();
env_vars.subMap(Eval.me(ENV_LIST)).each {
  new_parameters.add(new StringParameterValue(it.key, it.value));
}

def modified_parameters = null
def old_parameters = build.getAction(ParametersAction.class)
if (old_parameters != null) {
  build.actions.remove(old_parameters)
  modified_parameters = old_parameters.createUpdated(new_parameters)
} else {
  modified_parameters = new ParametersAction(new_parameters)
}
build.actions.add(modified_parameters)
