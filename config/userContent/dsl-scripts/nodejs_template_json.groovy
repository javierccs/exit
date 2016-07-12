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
			"title": "Front build advanced options",
			"description" : "Front build customizable options. (Do not use quotes).",
			"type": "object",
			"options": {
			  "collapsed": true
			},
			"properties": {
				"DIST_INCLUDE": {
					"type": "string",
					"description": "Files inside Distribution directory to include. By default all files are included (i.e.  &quot;&#42; &quot;,  &quot;&#42;.html &quot;,...)",
					"default": "*",
					"propertyOrder": 1
				},
				"DIST_EXCLUDE": {
					"type": "string",
					"description": "Files inside Distribution directory to exclude. By default all files are included (i.e.  &quot;.&#42; &quot;,  &quot;README&#42; &quot;,...)",
					"default": "",
					"propertyOrder": 2
				},  
				"JUNIT_TESTS_PATTERN": {
					"type": "string",
					"description": "Test results file pattern (i.e. PhantomJS&#42;&#47;&#42;.xml)",
					"default": "",
					"propertyOrder": 3
				},
				"CONFIG_DIRECTORY": {
					"type": "string",
					"description": "Config files directory to archive. This files will be passed to docker image if additional webserver configuration is desired (i.e. ngnix-conf)",
					"default": "",
					"propertyOrder": 4
				},
				"TZ": {
					"type": "string",
					"description": "TimeZone for the running containers",
					"default": "",
					"propertyOrder": 5
				}
    
			}
		}
	}
/);
