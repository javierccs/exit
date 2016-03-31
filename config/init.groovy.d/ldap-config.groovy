import jenkins.model.*
import hudson.security.*
import java.util.logging.Logger
import hudson.util.Secret

def logger = Logger.getLogger('hudson.security.LDAPSecurityRealm')
String server = System.getenv('LDAP_SERVER')
String userSearchBase = System.getenv('LDAP_BASE')
String groupSearchBase = System.getenv('LDAP_GROUP_BASE')
String managerDN = System.getenv('LDAP_BIND_DN')
String managerPassword = System.getenv('LDAP_PASSWORD')

if (!(server?.trim() && userSearchBase && groupSearchBase
    && managerDN && managerPassword)) {
  logger.warning("LDAP environment variables are not set")
  return
}

boolean inhibitInferRootDN = true
boolean disableMailAddressResolver = false
String displayNameAttributeName = 'cn'
String mailAddressAttributeName = 'mail'

SecurityRealm ldap_realm = new LDAPSecurityRealm(server, '', userSearchBase, '', groupSearchBase, '', null, managerDN, Secret.fromString(managerPassword), inhibitInferRootDN, disableMailAddressResolver, null, null, displayNameAttributeName, mailAddressAttributeName)
Jenkins.instance.setSecurityRealm(ldap_realm)

//AuthorizationStrategy strategy = new FullControlOnceLoggedInAuthorizationStrategy()
//Jenkins.instance.setAuthorizationStrategy(strategy)
Jenkins.instance.save()
