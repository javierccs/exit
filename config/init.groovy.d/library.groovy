import jenkins.model.*
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever
import jenkins.plugins.git.GitSCMSource
def inst = Jenkins.getInstance()
def libraryVersion = System.getenv("SERENITY_ALM_LIBRARY_VERSION")
if (libraryVersion != null && libraryVersion.trim()){
  libraryVersion = libraryVersion.trim()
  def globalLibraries = inst.getDescriptor("org.jenkinsci.plugins.workflow.libs.GlobalLibraries")
  def libraryRepoUrl = System.getenv("SERENITY_ALM_LIBRARY_REPOSITORY")
  if (libraryRepoUrl == null || !libraryRepoUrl.trim()) {
    def gitURL = System.getenv("GITLAB_URL").trim()
    if (!gitURL.endsWith('/')){
      gitURL += '/'
    }
    libraryRepoUrl = "${gitURL}serenity-alm/serenity-alm-jenkins-library.git"
  }
  GitSCMSource gs = new GitSCMSource("serenity-alm-library",
    libraryRepoUrl, null, "*", "", true)

  SCMSourceRetriever sourceRetriever = new SCMSourceRetriever (gs)
  LibraryConfiguration lc = new LibraryConfiguration("alm",sourceRetriever )
  lc.implicit = true
  lc.allowVersionOverride = false
  lc.defaultVersion = libraryVersion
  List<LibraryConfiguration> libraries = new ArrayList<>()
  libraries.add(lc)
  globalLibraries.setLibraries (libraries)
}else{
  println ("[WARN] Library Version variable 'SERENITY_ALM_LIBRARY_VERSION' not set. Serenity ALM Library won't be set")
}

