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
			"description": "Wordpress database host",
			"default": "external-mysql:3306"
		},  
		"WORDPRESS_DB_USER_PRO": {
			"type": "string",
			"description": "Wordpress database user",
			"default": "admin"
		}, 
		"WORDPRESS_DB_PASSWORD_PRO": {
			"type": "string",
			"description": "Wordpress database password",
			"default": "aquielpassword"
		}, 
		"WORDPRESS_DB_NAME_PRO": {
			"type": "string",
			"description": "Wordpress database name",
			"default": "wordpress"
		},
		"S3_BACKUP_HOST_PRO": {
			"type": "string",
			"description": "S3 Backup Host",
		}, 
		"S3_BACKUP_BUCKET_PRO": {
			"type": "string",
			"description": "S3 Backup Bucket",
		},
		"S3_BACKUP_ACCESS_KEY_PRO": {
			"type": "string",
			"description": "S3 Backup access key",
		},
		"S3_BACKUP_SECRET_KEY_PRO": {
			"type": "string",
			"description": "S3 backup secret key",
		} 
    
      }
    
    }
}
/);
