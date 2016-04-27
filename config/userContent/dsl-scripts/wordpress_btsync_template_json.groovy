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
	  "title": "Wordpress OSE3 Template parameters - Development environment",
	  "description" : "Customizable Wordpress OSE3 template parameters (Do not use quotes). <br>The parameters in blank are leave with the PAAS's default values(consult PAAS page for more information)",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },	
	  "properties": {
 		"WORDPRESS_DB_HOST_DEV": {
			"type": "string",
			"description": "WordPress Database host:port",
			"default": "external-mysql:3306",
                        "propertyOrder": 1
		},  
		"WORDPRESS_DB_USER_DEV": {
			"type": "string",
			"description": "WordPress Database user.",
			"default": "admin",
                        "propertyOrder": 2

		}, 
		"WORDPRESS_DB_PASSWORD_DEV": {
			"type": "string",
			"description": "WordPress Database user password.",
			"default": "aquielpassword",
                        "propertyOrder": 3

		}, 
		"WORDPRESS_DB_NAME_DEV": {
			"type": "string",
			"description": "WordPress MySql Database name.",
			"default": "wordpress",
                        "propertyOrder": 4

		},
		"S3_BACKUP_HOST_DEV": {
			"type": "string",
			"description": "The S3 host fqdn where the backup is stored.",
                        "propertyOrder": 5

		}, 
		"S3_BACKUP_BUCKET_DEV": {
			"type": "string",
			"description": "The S3 bucket where the backup is stored. (format: s3:\/\/bucket)",
                        "propertyOrder": 6


		},
		"S3_BACKUP_ACCESS_KEY_DEV": {
			"type": "string",
			"description": "The S3 access key to download the backup.",
                        "propertyOrder": 7

		},
		"S3_BACKUP_SECRET_KEY_DEV": {
			"type": "string",
			"description": "The S3 secret key to download the backup.",
                        "propertyOrder": 8

		}, 
		"HTTP_PROXY_DEV": {
			"type": "string",
			"description": "Http Proxy environment variable.",
			"propertyOrder": 9

		}, 
		"HTTPS_PROXY_DEV": {
			"type": "string",
			"description": "Https Proxy environment variable.",
                        "propertyOrder": 10

		}, 
		"NO_PROXY_DEV": {
			"type": "string",
			"description": "No Proxy environment variable.",
			"default": "s3.boae.paas.gsnetcloud.corp",
                        "propertyOrder": 11

		},
		"TZ_DEV": {
			"type": "string",
			"description": "TimeZone for the running containers.",
                        "propertyOrder": 12

		},
		"IGNORELIST_DEV": {
			"type": "string",
			"description": "Bittorent Sync Ignore List.",
                        "propertyOrder": 13

		},
		"SECRETBTSYNC_DEV": {
			"type": "string",
			"description": "Secret Key for Bittorent Sync communication [A-Z0-9]{33}.", 
                        "propertyOrder": 14

		}
    
      }
    
    }
}
/);
