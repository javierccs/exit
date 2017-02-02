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
	  "title": "Liferay OSE3 Template parameters - Development environment",
	  "description" : "Customizable Liferay OSE3 template parameters (Do not use quotes). <br>The parameters in blank are leave with the PAAS's default values(consult PAAS page for more information)",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },	
	  "properties": {
 		"CONFIGURATION_GIT_DEV": {
			"type": "string",
			"description": "Git repository for Liferay configuration",
			"default": "",
                        "propertyOrder": 1
		},
		"CONFIGURATION_GIT_USR_DEV": {
			"type": "string",
			"description": "User of the git repository for Liferay configuration",
			"default": "",
                        "propertyOrder": 2
		},
		"CONFIGURATION_GIT_PASS_DEV": {
			"type": "string",
			"description": "Password or Token of the git repository for Liferay configuration",
			"default": "",
                        "propertyOrder": 3
		},
		"JAVA_OPTS_EXT_DEV": {
			"type": "string",
			"description": "Java options. If you want to redefine memory parameters write them here",
                        "propertyOrder": 4
		}, 
		"WILY_MOM_FQDN_DEV": {
			"type": "string",
			"description": "fully qualified domain name of the Wily Introscope MoM server",
			 "propertyOrder": 5
		},
		"WILY_MOM_PORT_DEV": {
			"type": "string",
			"description": "port of the Wily Introscope MoM server",
                        "propertyOrder": 6
		},
		"TZ_DEV": {
			"type": "string",
			"description": "TimeZone for the running containers.",
			"dafault": "Europe\/Madrid",
                        "propertyOrder": 7
		},
		"CONTAINER_MEMORY_DEV": {
			"type": "string",
			"description": "Maximum memory for Liferay (value in Megabytes. You should not exceed your quota)", 
			"default": "2048M",
                        "propertyOrder": 8
		},
		"VOLUME_CAPACITY_DEV": {
                        "type": "string",
                        "description": "Volume capacity available for content data (NFS size), e.g. 512Mi, 2Gi",
                        "default": "512Mi",
                        "propertyOrder": 9
                }
      }
    }
}
/);