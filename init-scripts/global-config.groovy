import hudson.model.*;
import jenkins.model.*;

def inst = Jenkins.getInstance()

println "--> disabling master executors"
inst.setNumExecutors(1)

def VIEW_NAME = 'Templates'
def view = inst.getView(VIEW_NAME)
if (view == null) {
  println '--> Create '+VIEW_NAME+' view'
  view = new ListView(VIEW_NAME)
  view.setIncludeRegex('TL-.*')
  inst.addView(view)
}
