import net.sf.json.JSONObject;

def jsonEditorOptions = JSONObject.fromObject(/{
		disable_edit_json: true,
        disable_properties: true,
        no_additional_properties: true,
        disable_array_add: true,
        disable_array_delete: true,
        disable_array_reorder: true,
        theme: "bootstrap3",
        iconlib:"foundation3",
		schema: {
	  "title": "Liferay OSE3 Template parameters - Preproduction environment",
	  "description" : "Customizable Liferay OSE3 template parameters (Do not use quotes). <br>The parameters in blank are leave with the PAAS's default values(consult PAAS page for more information)",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },	
	  "properties": {
		"CONFIGURATION_GIT_PRE": {
			"type": "string",
			"description": "Git repository for PHP and Apache configuration",
			"default": "",
                        "propertyOrder": 1
		},
		"CONFIGURATION_GIT_USR_PRE": {
			"type": "string",
			"description": "User of the git repository for PHP and Apache configuration",
			"default": "",
                        "propertyOrder": 2
		},
		"CONFIGURATION_GIT_PASS_PRE": {
			"type": "string",
			"description": "Password of the git repository for PHP and Apache configuration",
			"default": "",
                        "propertyOrder": 3
		},
		"JAVA_OPTS_EXT_PRE": {
			"type": "string",
			"description": "Java options. If you want to redefine memory parameters write them here",
                        "propertyOrder": 6
		}, 
		"WILY_MOM_FQDN_PRE": {
			"type": "string",
			"description": "fully qualified domain name of the Wily Introscope MoM server",
			 "propertyOrder": 7
		},
		"WILY_MOM_PORT_PRE": {
			"type": "string",
			"description": "port of the Wily Introscope MoM server",
                        "propertyOrder": 8
		},
		"HTTP_PROXY_PRE": {
			"type": "string",
			"description": "Http Proxy environment variable.",
			"propertyOrder": 10
		}, 
		"HTTPS_PROXY_PRE": {
			"type": "string",
			"description": "Https Proxy environment variable.",
                        "propertyOrder": 11
		}, 
		"NO_PROXY_PRE": {
			"type": "string",
			"description": "No Proxy environment variable.",
			"default": "s3.boae.paas.gsnetcloud.corp",
                        "propertyOrder": 12
		},
		"TZ_PRE": {
			"type": "string",
			"description": "TimeZone for the running containers.",
			"dafault": "Europe\/Madrid",
                        "propertyOrder": 13
		},
		"IGNORELIST_PRE": {
			"type": "string",
			"description": "Bittorent Sync Ignore List. (Use comma separator)",
                        "propertyOrder": 14
		},
		"CONTAINER_MEMORY_PRE": {
			"type": "string",
			"description": "Maximum memory for Btsync (value in Megabytes. You should not exceed your quota)", 
			"default": "1024M",
                        "propertyOrder": 16
		},
		"VOLUME_CAPACITY_PRE": {
                        "type": "string",
                        "description": "Volume capacity available for content data (NFS size), e.g. 512Mi, 2Gi",
                        "default": "512Mi",
                        "propertyOrder": 16
                }
      }
    
    }
}
/);
