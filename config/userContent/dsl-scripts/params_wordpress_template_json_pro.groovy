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
	  "title": "Wordpress OSE3 Template parameters - Production environment",
	  "description" : "Customizable Wordpress OSE3 template parameters (Do not use quotes). <br>The parameters in blank are leave with the PAAS's default values(consult PAAS page for more information)",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },	
	  "properties": {
		"OSE3_TOKEN_PROJECT_PRO": {
                      "type": "string",
                      "description": "This is the token OSE3 project",
                      "default": "CHANGEME",
                      "propertyOrder": 1
              },
 		"WORDPRESS_DB_HOST_PRO": {
			"type": "string",
			"description": "WordPress Database host:port",
			"default": "external-mysql:3306",
                        "propertyOrder": 2
		},  
		"WORDPRESS_DB_USER_PRO": {
			"type": "string",
			"description": "WordPress Database user.",
			"default": "admin",
                        "propertyOrder": 3
		}, 
		"WORDPRESS_DB_PASSWORD_PRO": {
			"type": "string",
			"description": "WordPress Database user password.",
			"default": "aquielpassword",
                        "propertyOrder": 4
		}, 
		"WORDPRESS_DB_NAME_PRO": {
			"type": "string",
			"description": "WordPress MySql Database name.",
			"default": "wordpress",
                        "propertyOrder": 5
		},
		"CONFIGURATION_GIT_PRO": {
			"type": "string",
			"description": "Git repository for PHP and Apache configuration",
			"default": "",
                        "propertyOrder": 6
		},
		"S3_BACKUP_HOST_PRO": {
			"type": "string",
			"description": "The S3 host fqdn where the backup is stored.",
                        "propertyOrder": 7
		}, 
		"S3_BACKUP_BUCKET_PRO": {
			"type": "string",
			"description": "The S3 bucket where the backup is stored. (format: s3:\/\/bucket)",
                        "propertyOrder": 8
		},
		"S3_BACKUP_ACCESS_KEY_PRO": {
			"type": "string",
			"description": "The S3 access key to download the backup.",
                        "propertyOrder": 9
		},
		"S3_BACKUP_SECRET_KEY_PRO": {
			"type": "string",
			"description": "The S3 secret key to download the backup.",
                        "propertyOrder": 10
		}, 
		"HTTP_PROXY_PRO": {
			"type": "string",
			"description": "Http Proxy environment variable.",
			"propertyOrder": 11
		}, 
		"HTTPS_PROXY_PRO": {
			"type": "string",
			"description": "Https Proxy environment variable.",
                        "propertyOrder": 12
		}, 
		"NO_PROXY_PRO": {
			"type": "string",
			"description": "No Proxy environment variable.",
			"default": "s3.boae.paas.gsnetcloud.corp",
                        "propertyOrder": 13
		},
		"TZ_PRO": {
			"type": "string",
			"description": "TimeZone for the running containers.",
			"dafault": "Europe\/Madrid",
                        "propertyOrder": 14
		},
		"IGNORELIST_PRO": {
			"type": "string",
			"description": "Bittorent Sync Ignore List. (Use comma separator)",
                        "propertyOrder": 15
		},
		"SECRETBTSYNC_PRO": {
			"type": "string",
			"description": "Secret Key for Bittorent Sync communication", 
                        "propertyOrder": 16
		},
		"CONTAINER_MEMORY_PRO": {
			"type": "string",
			"description": "Maximum memory for Btsync (value in Megabytes. You should not exceed your quota)", 
			"default": "512M",
                        "propertyOrder": 17
		},
		"BTSYNC_MEMORY_PRO": {
			"type": "string",
			"description": "Maximum memory for Btsync (value in Megabytes. You should not exceed your quota)",
			"default": "100M",                         
                        "propertyOrder": 18
		}
    
      }
    
    }
}
/);