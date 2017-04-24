import org.boon.Boon;

Boon.fromJson(/{
	disable_edit_json: true, disable_properties: true, no_additional_properties: true, disable_array_add: true, disable_collapse: true,
	disable_array_delete: true, disable_array_reorder: true, theme: "bootstrap3", iconlib: "foundicons2",
	schema: {
		title: "Front build options", description : "Front build customizable options. (Do not use quotes).", type: "object",
		properties: {
			COMPILER: {
				type: "string",
				description: "Static files compilation mode",
				enum: [ "None", "Grunt_Gulp", "NPM_run_build" ],
				options: { enum_titles: [ "None", "Grunt\/Gulp", "npm run build" ] },
				default: "NPM_run_build",
				propertyOrder: 1
			},
			JUNIT_TESTS_PATTERN: {
				type: "string",
				description: "Test results file pattern (i.e. PhantomJS*\/*.xml)",
				default: "",
				propertyOrder: 2
			},
			DIST_DIR: {
				type: "string",
				description: "Distribution directory (i.e. \"dist \", \". \",...)",
				default: "dist",
				propertyOrder: 3
			},
			DIST_INCLUDE: {
				type: "string",
				description: "Files inside Distribution directory to include. By default all files are included (i.e. \"* \", \"*.html \",...)",
				default: "*",
				propertyOrder: 4
			},
			DIST_EXCLUDE: {
				type: "string",
				description: "Files inside Distribution directory to exclude. By default all files are included (i.e. \".* \", \"README* \",...)",
				default: "",
				propertyOrder: 5
			},
			CONFIG_DIRECTORY: {
				type: "string",
				description: "Config files directory to archive. This files will be passed to docker image if additional webserver configuration is desired (i.e. ngnix-conf)",
				default: "",
				propertyOrder: 6
			}
		}
	}
}/);
