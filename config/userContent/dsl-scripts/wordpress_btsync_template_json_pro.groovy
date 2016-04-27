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
 		"WORDPRESS_DB_HOST_PRO": {
			"type": "string",
			"description": "WordPress Database host:port",
			"default": "external-mysql:3306"
		},  
		"WORDPRESS_DB_USER_PRO": {
			"type": "string",
			"description": "WordPress Database user.",
			"default": "admin"
		}, 
		"WORDPRESS_DB_PASSWORD_PRO": {
			"type": "string",
			"description": "WordPress Database user password.",
			"default": "aquielpassword"
		}, 
		"WORDPRESS_DB_NAME_PRO": {
			"type": "string",
			"description": "WordPress MySql Database name.",
			"default": "wordpress"
		},
		"S3_BACKUP_HOST_PRO": {
			"type": "string",
			"description": "The S3 host fqdn where the backup is stored."
		}, 
		"S3_BACKUP_BUCKET_PRO": {
			"type": "string",
			"description": "The S3 bucket where the backup is stored. (format: s3:\/\/bucket)"
		},
		"S3_BACKUP_ACCESS_KEY_PRO": {
			"type": "string",
			"description": "The S3 access key to download the backup."
		},
		"S3_BACKUP_SECRET_KEY_PRO": {
			"type": "string",
			"description": "The S3 secret key to download the backup."
		}, 
		"HTTP_PROXY_PRO": {
			"type": "string",
			"description": "Http Proxy environment variable."
		}, 
		"HTTPS_PROXY_PRO": {
			"type": "string",
			"description": "Https Proxy environment variable."
		}, 
		"NO_PROXY_PRO": {
			"type": "string",
			"description": "No Proxy environment variable.",
			"default": "s3.boae.paas.gsnetcloud.corp"
		},
		"TZ_PRO": {
			"type": "string",
			"description": "TimeZone for the running containers."
		},
		"IGNORELIST_PRO": {
			"type": "string",
			"description": "Bittorent Sync Ignore List."
		},
		"SECRETBTSYNC_PRO": {
			"type": "string",
			"description": "Secret Key for Bittorent Sync communication [A-Z0-9]{33}."
		}
    
      }
    
    }
}
/);
