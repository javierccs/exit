import jenkins.model.*
import hudson.security.*
import com.michelin.cio.hudson.plugins.rolestrategy.*
import java.util.logging.Logger
import hudson.util.Secret

def logger = Logger.getLogger('hudson.security.LDAPSecurityRealm')
String server = System.getenv('LDAP_SERVER')
String userSearchBase = System.getenv('LDAP_BASE')
String groupSearchBase = System.getenv('LDAP_GROUP_BASE')
String groupSearchFilter = System.getenv('LDAP_GROUP_FILTER')
String managerDN = System.getenv('LDAP_BIND_DN')
String managerPassword = System.getenv('LDAP_PASSWORD')

if (!(server?.trim() && userSearchBase?.trim() && groupSearchBase?.trim()
    && groupSearchFilter?.trim() && managerDN?.trim() && managerPassword?.trim())) {
  logger.warning("LDAP environment variables are not set")
  return
}

boolean inhibitInferRootDN = true
boolean disableMailAddressResolver = false
String displayNameAttributeName = 'cn'
String mailAddressAttributeName = 'mail'

SecurityRealm ldap_realm = new LDAPSecurityRealm(server, '', userSearchBase, '', groupSearchBase, groupSearchFilter, null, managerDN, Secret.fromString(managerPassword), inhibitInferRootDN, disableMailAddressResolver, null, null, displayNameAttributeName, mailAddressAttributeName)
Jenkins.instance.setSecurityRealm(ldap_realm)

def list = System.getenv('LDAP_GROUPS')
def ROLE_NAME = 'user'
def realm = Jenkins.instance.getAuthorizationStrategy()
if (!(list?.trim() && realm instanceof RoleBasedAuthorizationStrategy)) {
  logger.warning("RoleBasedAuthorizationStrategy not found or empty LDAP_GROUPS environment variable")
  return false
}
def role = realm.getGrantedRoles(realm.GLOBAL).keySet().find() {ROLE_NAME.equals(it.getName())}
if (role == null)  {
  logger.severe("Role $ROLE_NAME not found.")
  return false
}
list.split(',').each {
  realm.assignRole(realm.GLOBAL, role, it.trim())
}
Jenkins.instance.save()
