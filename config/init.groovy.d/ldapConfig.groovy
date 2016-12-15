import jenkins.model.*
import hudson.security.*
import com.michelin.cio.hudson.plugins.rolestrategy.*
import java.util.logging.Logger
import hudson.util.Secret

 /* Add a set of user groups to a set of roles */
 def addRoles(roleBasedStrategy, roleBasedType, arrayGroups, arrayRoles) {
   def logger = Logger.getLogger('hudson.security.LDAPSecurityRealm')
   arrayRoles.each { roleName ->
	
     def role = roleBasedStrategy.getGrantedRoles(roleBasedType).keySet().find() {roleName.equals(it.getName())}
     assert (role != null) : logger.severe("Role $roleName not found.")
     arrayGroups.split(',').each {
       roleBasedStrategy.assignRole(roleBasedType, role, it.trim())
       logger.info("$it group added to $roleName role")
     }
   }
 }



def logger = Logger.getLogger('hudson.security.LDAPSecurityRealm')

String adminGroups = System.getenv('LDAP_ADMIN_GROUPS') ?: 'jenkins-administrators-tenant'
String userGroups = System.getenv('LDAP_USER_GROUPS') ?: System.getenv('LDAP_GROUPS')
String promoterGroups = System.getenv('LDAP_PROMOTER_GROUPS') ?: 'impes-product-owner,impes-technical-lead,impes-developer'

String server = System.getenv('LDAP_SERVER')
String userSearchBase = System.getenv('LDAP_BASE')
String groupSearchBase = System.getenv('LDAP_GROUP_BASE')
String groupSearchFilter = System.getenv('LDAP_GROUP_FILTER')
String managerDN = System.getenv('LDAP_BIND_DN')
String managerPassword = System.getenv('LDAP_PASSWORD')
String userSearchFilter = System.getenv('LDAP_USER_SEARCH')
String displayNameAttributeName = System.getenv('LDAP_DISPLAY_NAME')

if (!(server?.trim() && userSearchBase?.trim() && groupSearchBase?.trim()
    && groupSearchFilter?.trim() && managerDN?.trim() && managerPassword?.trim()
    && adminGroups?.trim() && userGroups?.trim() && promoterGroups?.trim())) {
  logger.warning("LDAP environment variables are not set")
  return
}

if ( !(userSearchFilter?.trim() )) {
  userSearchFilter = ''
}

if ( !(displayNameAttributeName?.trim() )) {
  displayNameAttributeName = 'cn'
}

boolean inhibitInferRootDN = true
boolean disableMailAddressResolver = false

String mailAddressAttributeName = 'mail'

SecurityRealm ldap_realm = new LDAPSecurityRealm(server, '', userSearchBase, userSearchFilter, groupSearchBase, groupSearchFilter, null, managerDN, Secret.fromString(managerPassword), inhibitInferRootDN, disableMailAddressResolver, null, null, displayNameAttributeName, mailAddressAttributeName)
Jenkins.instance.setSecurityRealm(ldap_realm)

logger.info("LDAP environment variables are set. Using LDAP security realm.")
 
def realm = Jenkins.instance.getAuthorizationStrategy()
assert (realm instanceof RoleBasedAuthorizationStrategy) : logger.severe("RoleBasedAuthorizationStrategy not found")
 
addRoles(realm, realm.GLOBAL, adminGroups, ['admin'])
addRoles(realm, realm.GLOBAL, promoterGroups, ['promoter'])
addRoles(realm, realm.GLOBAL, userGroups, ['user'])
addRoles(realm, realm.PROJECT, userGroups, ['ci_user','dev_user','tl_user','utl_user'])
addRoles(realm, realm.PROJECT, promoterGroups, ['pre_user','pro_user','tl_token-management'])

Jenkins.instance.save()
