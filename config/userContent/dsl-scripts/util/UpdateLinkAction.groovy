/*** BEGIN META {
  "name" : "Update LinkAction url from environment variable",
  "parameters" : LINK_URL,
  "authors" : [
    { name : "fdelamor@isban.es" }
  ]
} END META**/
import hudson.plugins.sidebar_link.LinkAction

def inst = build.getProject().getAction(LinkAction.class)
if (inst != null) {
  inst.url = build.getEnvironment(listener).get(binding.variables.get('LINK_URL'))
}
