import jenkins.model.*;

String no_spaces(value){
    return value.trim();
}

String no_spaces_and_lowercase(value){
    return no_spaces(value).toLowerCase();
}

//Retrieve execution input parameters
def inputData() {
    return [
            gitLabProject: no_spaces("${GITLAB_PROJECT}"),
            gitLabReleaseBranch: no_spaces("${GIT_RELEASE_BRANCH}"),
            gitLabIntegrationBranch: no_spaces("${GIT_INTEGRATION_BRANCH}"),
            openShiftUrl: no_spaces("${OSE3_URL}"),
            openShiftProjectName: no_spaces_and_lowercase("${OSE3_PROJECT_NAME}"),
            openShiftCredentials: "${SERENITY_CREDENTIAL}"
    ];
}

//Invoke method that return a map with the build parameters.
def params = inputData();

println "Params: $params";




