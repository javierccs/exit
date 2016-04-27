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
 		"WORDPRESS_DB_HOST_PRE": {
			"type": "string",
			"description": "WordPress Database host:port",
			"default": "external-mysql:3306"
		},  
		"WORDPRESS_DB_USER_PRE": {
			"type": "string",
			"description": "WordPress Database user.",
			"default": "admin"
		}, 
		"WORDPRESS_DB_PASSWORD_PRE": {
			"type": "string",
			"description": "WordPress Database user password.",
			"default": "aquielpassword"
		}, 
		"WORDPRESS_DB_NAME_PRE": {
			"type": "string",
			"description": "WordPress MySql Database name.",
			"default": "wordpress"
		},
		"S3_BACKUP_HOST_PRE": {
			"type": "string",
			"description": "The S3 host fqdn where the backup is stored.",
		}, 
		"S3_BACKUP_BUCKET_PRE": {
			"type": "string",
			"description": "The S3 bucket where the backup is stored. (format: s3:\/\/bucket)",
		},
		"S3_BACKUP_ACCESS_KEY_PRE": {
			"type": "string",
			"description": "The S3 access key to download the backup.",
		},
		"S3_BACKUP_SECRET_KEY_PRE": {
			"type": "string",
			"description": "The S3 secret key to download the backup.",
		}, 
		"HTTP_PROXY_PRE": {
			"type": "string",
			"description": "Http Proxy environment variable.",
			"default": "http:\/\/proxy.lvtc.gsnet.corp:80"
		}, 
		"HTTPS_PROXY_PRE": {
			"type": "string",
			"description": "Https Proxy environment variable.",
			"default": "http:\/\/proxy.lvtc.gsnet.corp:80"
		}, 
		"NO_PROXY_PRE": {
			"type": "string",
			"description": "No Proxy environment variable.",
			"default": "s3.boae.paas.gsnetcloud.corp"
		},
		"TZ_PRE": {
			"type": "string",
			"description": "TimeZone for the running containers.",
		},
		"IGNORELIST_PRE": {
			"type": "string",
			"description": "Bittorent Sync Ignore List.",
		},
		"SECRETBTSYNC_PRE": {
			"type": "string",
			"description": "Secret Key for Bittorent Sync communication [A-Z0-9]{33}.",
		}
    
      }
    
    }
}
/);
