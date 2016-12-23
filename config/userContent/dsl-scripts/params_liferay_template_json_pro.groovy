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
	  "title": "Liferay OSE3 Template parameters - Production environment",
	  "description" : "Customizable Liferay OSE3 template parameters (Do not use quotes). <br>The parameters in blank are leave with the PAAS's default values(consult PAAS page for more information)",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },	
	  "properties": {
 		"CONFIGURATION_GIT_PRO": {
			"type": "string",
			"description": "Git repository for PHP and Apache configuration",
			"default": "",
                        "propertyOrder": 1
		},
		"CONFIGURATION_GIT_USER_PRO": {
			"type": "string",
			"description": "User of the git repository for PHP and Apache configuration",
			"default": "",
                        "propertyOrder": 2
		},
		"CONFIGURATION_GIT_PASSWORD_PRO": {
			"type": "string",
			"description": "Password of the git repository for PHP and Apache configuration",
			"default": "",
                        "propertyOrder": 3
		},
		"S3_BACKUP_HOST_PRO": {
			"type": "string",
			"description": "The S3 host fqdn where the backup is stored.",
                        "propertyOrder": 6
		}, 
		"S3_BACKUP_BUCKET_PRO": {
			"type": "string",
			"description": "The S3 bucket where the backup is stored. (format: s3:\/\/bucket)",
                        "propertyOrder": 7
		},
		"S3_BACKUP_ACCESS_KEY_PRO": {
			"type": "string",
			"description": "The S3 access key to download the backup.",
                        "propertyOrder": 8
		},
		"S3_BACKUP_SECRET_KEY_PRO": {
			"type": "string",
			"description": "The S3 secret key to download the backup.",
                        "propertyOrder": 9
		}, 
		"HTTP_PROXY_PRO": {
			"type": "string",
			"description": "Http Proxy environment variable.",
			"propertyOrder": 10
		}, 
		"HTTPS_PROXY_PRO": {
			"type": "string",
			"description": "Https Proxy environment variable.",
                        "propertyOrder": 11
		}, 
		"NO_PROXY_PRO": {
			"type": "string",
			"description": "No Proxy environment variable.",
			"default": "s3.boae.paas.gsnetcloud.corp",
                        "propertyOrder": 12
		},
		"TZ_PRO": {
			"type": "string",
			"description": "TimeZone for the running containers.",
			"dafault": "Europe\/Madrid",
                        "propertyOrder": 13
		},
		"IGNORELIST_PRO": {
			"type": "string",
			"description": "Bittorent Sync Ignore List. (Use comma separator)",
                        "propertyOrder": 14
		},
		"VOLUME_CAPACITY_DEV": {
			"type": "string",
			"description": "Maximum memory for Btsync (value in Megabytes. You should not exceed your quota)", 
			"default": "2048",
                        "propertyOrder": 16
		}
		"CONTAINER_MEMORY_PRO": {
			"type": "string",
			"description": "Maximum memory for Btsync (value in Megabytes. You should not exceed your quota)", 
			"default": "2048",
                        "propertyOrder": 16
		},
		    "VOLUME_CAPACITY_PRO": {
                        "type": "string",
                        "description": "Volume capacity available for content data (NFS size), e.g. 512Mi, 2Gi",
                        "default": "512Mi",
                        "propertyOrder": 16
                }
      }
    
    }
}
/);
