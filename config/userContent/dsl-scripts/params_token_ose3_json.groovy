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
 		"TOKEN_PROJECT_OSE3_DEV": {
			"type": "string",
			"description": "This is the token OSE3 project. Click here in order to know to generate it",
			"default": "CHANGEME",
                        "propertyOrder": 1
		},
 		"TOKEN_PROJECT_OSE3_PRE": {
			"type": "string",
			"description": "This is the token OSE3 project. Click here in order to know to generate it",
			"default": "CHANGEME",
                        "propertyOrder": 2
		},
 		"TOKEN_PROJECT_OSE3_PRO": {
			"type": "string",
			"description": "This is the token OSE3 project. Click here in order to know to generate it",
			"default": "CHANGEME",
                        "propertyOrder": 3
		}
	    
      }
    
    }
}
/);
