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
	  "title": "JAVASE OSE3 Template parameters",
	  "description" : "Customizable JAVASE OSE3 tempalte parameters (Do not use quotes). <br>The parameters in blank are leave with the PAAS's default values(consult PAAS page for more information)",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },
	  "properties": {
		"JAVA_OPTS_EXT": {
			"type": "string",
			"description": "Java options"
		},
		"JAVA_PARAMETERS": {
			"type": "string",
			"description": "Application java parameters"
		},
 		"POD_MAX_MEM": {
			"type": "string",
			"description": "Maximum memory for the pods (in Megabytes)"
		},  
		"TZ": {
			"type": "string",
			"description": "TimeZone for the running containers"
		}, 
		"WILY_MOM_FQDN": {
			"type": "string",
			"description": "fully qualified domain name of the Wily Introscope MoM server"
		}, 
		"WILY_MOM_PORT": {
			"type": "string",
			"description": "port of the Wily Introscope MoM server"
		}
    
      }
    
    }
}
/);
