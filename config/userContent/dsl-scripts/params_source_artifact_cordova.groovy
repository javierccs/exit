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
			"title": "Source artifact properties",
			"description" : "Fill this information in order to download artifact to Nexus repository. (Do not use quotes).",
			"type": "object",
			"options": {
			  "collapsed": false
			},
			"properties":  { 
				"ARTIFACT_NAME": {
					"type": "string",
					"description": "Artifact Name",
					"default": "",
					"propertyOrder": 1
				},  
				"ARTIFACT_VERSION": {
					"type": "string",
					"description": "Artifact name",
					"default": "",
					"propertyOrder": 2
				},
				"GROUP_PROJECT_GITLAB": {
					"type": "string",
					"description": "group\\/project gitlab",
					"default": "serenity-alm\\/www",
					"propertyOrder": 3
				}
    
			}
		}
	}
/);
