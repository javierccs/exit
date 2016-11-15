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
			"title": "APK artifact properties",
			"description" : "Fill this information in order to upload this apk to Nexus repository. (Do not use quotes).",
			"type": "object",
			"options": {
			  "collapsed": false
			},
			"properties": {  
				"APK_GROUPID": {
					"type": "string",
					"description": "Group Name of this artifact",
					"default": "com.serenity",
					"propertyOrder": 1
				},  
				"APK_ARTIFACTID": {
					"type": "string",
					"description": "Artifact name",
					"default": "android-debug",
					"propertyOrder": 2
				},
				"APK_VERSION": {
					"type": "string",
					"description": "Version of this artifact",
					"default": "0.1",
					"propertyOrder": 3
				}
    
			}
		}
	}
/);
