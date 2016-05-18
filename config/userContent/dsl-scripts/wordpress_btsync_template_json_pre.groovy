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
	  "title": "Wordpress OSE3 Template parameters - Preproduction environment",
	  "description" : "Customizable Wordpress OSE3 template parameters (Do not use quotes). <br>The parameters in blank are leave with the PAAS's default values(consult PAAS page for more information)",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },	
	  "properties": {
 		"WORDPRESS_DB_HOST_PRE": {
			"type": "string",
			"description": "WordPress Database host:port",
			"default": "external-mysql:3306",
                        "propertyOrder": 1
		},  
		"WORDPRESS_DB_USER_PRE": {
			"type": "string",
			"description": "WordPress Database user.",
			"default": "admin",
                        "propertyOrder": 2
		}, 
		"WORDPRESS_DB_PASSWORD_PRE": {
			"type": "string",
			"description": "WordPress Database user password.",
			"default": "aquielpassword",
                        "propertyOrder": 3
		}, 
		"WORDPRESS_DB_NAME_PRE": {
			"type": "string",
			"description": "WordPress MySql Database name.",
			"default": "wordpress",
                        "propertyOrder": 4
		},
		"CONFIGURATION_GIT_PRE": {
			"type": "string",
			"description": "Git repository for PHP and Apache configuration",
			"default": "",
                        "propertyOrder": 5
		},
		"S3_BACKUP_HOST_PRE": {
			"type": "string",
			"description": "The S3 host fqdn where the backup is stored.",
                        "propertyOrder": 6
		}, 
		"S3_BACKUP_BUCKET_PRE": {
			"type": "string",
			"description": "The S3 bucket where the backup is stored. (format: s3:\/\/bucket)",
                        "propertyOrder": 7
		},
		"S3_BACKUP_ACCESS_KEY_PRE": {
			"type": "string",
			"description": "The S3 access key to download the backup.",
                        "propertyOrder": 8
		},
		"S3_BACKUP_SECRET_KEY_PRE": {
			"type": "string",
			"description": "The S3 secret key to download the backup.",
                        "propertyOrder": 9
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
		"SECRETBTSYNC_PRE": {
			"type": "string",
			"description": "Secret Key for Bittorent Sync communication", 
                        "propertyOrder": 15
		},
		"CONTAINER_MEMORY_PRE": {
			"type": "string",
			"description": "Maximum memory for Btsync (value in Megabytes. You should not exceed your quota)", 
			"default": "512M",
                        "propertyOrder": 16
		},
		"BTSYNC_MEMORY_PRE": {
			"type": "string",
			"description": "Maximum memory for Btsync (value in Megabytes. You should not exceed your quota)",
			"default": "100M",                         
                        "propertyOrder": 17
		}
    
      }
    
    }
}
/);
