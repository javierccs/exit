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
	  "title": "Functional test",
	  "type": "object",
		 "options": {
			  "collapsed": true
		   },
	  "properties": {
		"ADD_HPALM_AT_DEV": {
			"type": "boolean",			
			"format": "checkbox",
			 "default": false,
			 "description": "The functional test are executed after deploy in development environement"			 
		},
		"ADD_HPALM_AT_PRE": {
			"type": "boolean",			
			"format": "checkbox",
			"default": false,
			"description": "The functional test are executed after deploy in QA (PRE) environement"
		},
		"HPALM_TEST_SET_ID": {
			"type": "string",
			"description": "Test Set ID from HP ALM",
			 "default": "901"
		},
		"HPALM_DOMAIN": {
			"type": "string",
			"description": "HP ALM domain",
			 "default": "METODOLOGIA_ISBAN"
		},
 		"HPALM_PROJECT": {
			"type": "string",
			"description": "HP ALM project",
			 "default": "NUEVAS_FUNC_ALM11"
		},  
		"HPALM_URL": {
			"type": "string",
			"description": "URL of HP ALM",
			 "default": "http:\/\/alm1.produban.gs.corp"
		}, 
		"GITLAB_PROJECT_TEST": {
			"type": "string",
			"description": "(Optional) GITLAB project for functional test. If it is passed blank the GITLAB project of the aplication will be used"
		}, 
		"URL_BASE_SELENIUM": {
			"type": "string",
			"description": "(Optional) URL where the functional test are executed (selenium case). If it is passed blank it will use the PAAS deployer URL"
		}
    
      }
    
    }
}
/);
