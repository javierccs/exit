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
	  "title": "OSE3 Token Management",
	  "description" : "Please include token value for each Openshift project (Do not use quotes)",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },	
	  "properties": {
 		"OSE3_TOKEN_PROJECT_DEV": {
			"type": "string",
			"description": "This is the token OSE3 project.",
			"default": "CHANGEME",
                        "propertyOrder": 1
		},
 		"OSE3_TOKEN_PROJECT_PRE": {
			"type": "string",
			"description": "This is the token OSE3 project.",
			"default": "CHANGEME",
                        "propertyOrder": 2
		},
 		"OSE3_TOKEN_PROJECT_PRO": {
			"type": "string",
			"description": "This is the token OSE3 project.",
			"default": "CHANGEME",
                        "propertyOrder": 3
		}
	    
      }
    
    }
}
/);
