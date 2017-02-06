import org.boon.Boon;

Boon.fromJson(/{
	disable_collapse: true, disable_edit_json: true,
	disable_properties: true, no_additional_properties: true,
	disable_array_add: false, disable_array_delete: false, disable_array_reorder: true,
	theme: "bootstrap2", iconlib:"foundicons2",
	schema: {
		title: "OpenShift Project", headerTemplate: "{{title}}: {{self.name}}", type: "object",
		properties : {
		    name: {title: "OpenShift project name (Openshift project name excluding the suffix '-dev')", type: "string", propertyOrder: 1 },
			region: {
				type: "string", propertyOrder: 2,
				enum: [
					https:\/\/api.boaw.paas.gsnetcloud.corp:8443,
					https:\/\/api.boae.paas.gsnetcloud.corp:8443,
					https:\/\/api.cto2.paas.gsnetcloud.corp:8443,
					https:\/\/api.cmpn.paas.gsnetcloud.corp:8443,
					https:\/\/api.cap1.paas.gsnetcloud.corp:8443
				],
				options: {
					enum_titles: [
						"BOAW",
						"BOAE",
						"CTO2",
						"CMPN",
						"CAP1"
					]
				},
			},
		    environments: {
				title: "Environments", type: "array", format: "tabs",
				options: { disable_array_add: true, disable_array_delete: true},
				items: {
					title: "Environment", headerTemplate: "{{self.name}}", type: "object",
					properties: {
						name: { title: "environment (OpenShift project environment)", type: "string", readOnly: true, propertyOrder: 1},
						template: { title: "Template name (OpenShift template to be used in deployment)", type: "string", propertyOrder: 3},
						parameters: {
							type: "array", format: "table", propertyOrder: 10,
							items: {
								type: "object",
								properties: {
									name: { type: "string" },
									value : { type: "string" }
								}
							}
						}
					}
				}
			}
		}
	},
	startval: ${STARTVAL}
}/);
