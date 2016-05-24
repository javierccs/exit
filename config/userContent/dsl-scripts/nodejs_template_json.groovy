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
				"DIST_DIR": {
					"type": "string",
					"description": "Ditribution directory (i.e.  &quot;dist &quot;,  &quot;. &quot;,...)",
					"default": "dist",
					"propertyOrder": 1
				},
				"DIST_INCLUDE": {
					"type": "string",
					"description": "Files inside Distribution directory to include. By default all files are included (i.e.  &quot;&#42; &quot;,  &quot;&#42;.html &quot;,...)",
					"default": "*",
					"propertyOrder": 2
				},
				"DIST_EXCLUDE": {
					"type": "string",
					"description": "Files inside Distribution directory to exclude. By default all files are included (i.e.  &quot;.&#42; &quot;,  &quot;README&#42; &quot;,...)",
					"default": "",
					"propertyOrder": 3
				},  
				"JUNIT_TESTS_PATTERN": {
					"type": "string",
					"description": "Test results file pattern (i.e. PhantomJS&#42;&#47;&#42;.xml)",
					"default": "",
					"propertyOrder": 4
				},
				"OSE3_TEMPLATE_NAME": {
					"type": "string",
					"description": "OpenShift template to be used in deployment",
					"default": "nginx-alm-1.0",
					"propertyOrder": 5
				},
				"TZ": {
					"type": "string",
					"description": "TimeZone for the running containers",
					"default": "",
					"propertyOrder": 6
				}
    
			}
		}
	}
/);
