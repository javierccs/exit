def GITLAB_PROJECT_SOURCE = "${GITLAB_PROJECT_SOURCE}".trim()

out.println("GitLab URL: " + GITLAB_PROJECT_SOURCE);

label('wordpress-docker')