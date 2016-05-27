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
			"title": "APIC advanced options",
			"description" : "API Connection customizable options. (Do not use quotes).",
			"type": "object",
			"options": {
			  "collapsed": true
			},
			"properties": {
				"APIC_DEV_CATALOG": {
					"type": "string",
					"description": "Development catalog name.",
					"default": "sb",
					"propertyOrder": 1
				},
				"APIC_PRE_CATALOG": {
					"type": "string",
					"description": "Pre-production catalog name.",
					"default": "pre",
					"propertyOrder": 2
				},
				"APIC_PRO_CATALOG": {
					"type": "string",
					"description": "Production catalog name.",
					"default": "pro",
					"propertyOrder": 3
				},
				"APIC_SRC_DIRECTORY": {
					"type": "string",
					"description": "Project source directory",
					"default": "src\/api",
					"propertyOrder": 4
				}
				
			}
		}
	}
/);
