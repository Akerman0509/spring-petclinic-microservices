pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    parameters {
        string(name: 'CUSTOMERS_SERVICE_BRANCH', defaultValue: 'main', description: 'Branch for customers-service')
        string(name: 'VISITS_SERVICE_BRANCH', defaultValue: 'main', description: 'Branch for visits-service')
        string(name: 'VETS_SERVICE_BRANCH', defaultValue: 'main', description: 'Branch for vets-service')
        string(name: 'GENAI_SERVICE_BRANCH', defaultValue: 'main', description: 'Branch for genai-service')
    }

    environment {
        WORKSPACE = "${env.WORKSPACE}"
        // List of services without test folders       
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
        // Full list of services
        ALL_SERVICES = "spring-petclinic-admin-server spring-petclinic-api-gateway spring-petclinic-config-server spring-petclinic-customers-service spring-petclinic-discovery-server spring-petclinic-genai-service spring-petclinic-vets-service spring-petclinic-visits-service"
        // Docker Hub credentials
        DOCKER_HUB_CREDS = credentials('docker-hub-credentials')
    }
    stages {
        stage('Print Parameters') {
            steps {
                echo "customers-service: ${params.CUSTOMERS_SERVICE_BRANCH}"
                echo "visits-service: ${params.VISITS_SERVICE_BRANCH}"
                echo "vets-service: ${params.VETS_SERVICE_BRANCH}"
                echo "genai-service: ${params.GENAI_SERVICE_BRANCH}"
            }
        }
        stage('Detect Branch and Changes') {
            steps {
                script {
                    echo "Running pipeline for Branch: ${env.BRANCH_NAME}"
                    
                    // Check if this is the main branch
                    if (env.BRANCH_NAME == 'main') {
                        echo "This is the main branch - will build all services"
                        env.CHANGED_SERVICES = env.ALL_SERVICES
                    } else {
                        // For non-main branches, detect changes
                        // Get changed files between current and previous commit
                        def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                        
                        // Define service directories to monitor
                        def services = env.ALL_SERVICES.split(" ")
                        
                        // Identify which services have changes
                        env.CHANGED_SERVICES = ""
                        for (service in services) {
                            if (changedFiles.contains(service)) {
                                env.CHANGED_SERVICES = env.CHANGED_SERVICES + " " + service
                            }
                        }
                        // If no specific service changes detected, check for common changes
                        if (env.CHANGED_SERVICES == "") {
                            if (changedFiles.contains("pom.xml") || 
                                changedFiles.contains(".github") || 
                                changedFiles.contains("docker-compose") ||
                                changedFiles.contains("Jenkinsfile")) {
                                echo "Common files changed, will build all services"
                                env.CHANGED_SERVICES = env.ALL_SERVICES
                            } else {
                                echo "No relevant changes detected"
                            }
                        }
                        
                        // Store changed files for detailed reporting
                        env.CHANGED_FILES = changedFiles
                    }
                    
                    echo "Services to build: ${env.CHANGED_SERVICES}"
                    
                    // Get the latest commit ID for tagging Docker images
                    env.COMMIT_ID = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    echo "Commit ID for Docker tags: ${env.COMMIT_ID}"
                }
                
                publishChecks name: 'Detect Changes', status: 'COMPLETED', conclusion: 'SUCCESS', 
                    summary: env.BRANCH_NAME == 'main' ? "Building all services on main branch" : "Changed files detected: ${env.CHANGED_FILES?.split('\n')?.size() ?: 0}",
                    text: env.BRANCH_NAME == 'main' ? 
                        """## Main Branch Build
                        Building all services because this is the main branch.""" :
                        """## Changed Files
                        ```
                        ${env.CHANGED_FILES ?: 'No changes detected'}
                        ```
                        
                        ## Services to Build
                        ```
                        ${env.CHANGED_SERVICES ?: 'No services to build'}
                        ```"""
            }
            post {
                failure {
                    publishChecks name: 'Detect Changes', status: 'COMPLETED', conclusion: 'FAILURE', 
                        summary: 'Failed to detect changes'
                }
            }
        }
        
        stage('Build and Push Docker Images') {
            when {
                expression { return env.CHANGED_SERVICES?.trim() }
            }
            steps {
                script {
                    // Login to Docker Hub using withCredentials for proper secret handling
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', 
                                                    usernameVariable: 'DOCKER_USERNAME', 
                                                    passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin"
                        
                        // Build each changed service
                        def services = env.CHANGED_SERVICES.trim().split(" ")
                        for (service in services) {
                            echo "Building Docker image for ${service}"
                            
                            // Build the service and its Docker image
                            sh "./mvnw clean install -PbuildDocker -pl ${service}"
                            
                            // Get image info (assuming the image is named the same as the service directory)
                            // def imageName = service.replace("spring-petclinic-", "")
                            def baseImageName = "\$DOCKER_USERNAME/${service}"
                            
                            // Tag the image with commit ID
                            sh "docker tag ${baseImageName}:latest ${baseImageName}:${env.COMMIT_ID}"
                            
                            // Push both tags to Docker Hub
                            echo "Pushing ${baseImageName}:latest and ${baseImageName}:${env.COMMIT_ID} to Docker Hub"
                            sh "docker push ${baseImageName}:latest"
                            sh "docker push ${baseImageName}:${env.COMMIT_ID}"
                        }
                    }
                }
            }
            // ...post actions remain the same
        }
        stage('Clean') {
            steps {
                echo 'Cleaning workspace and build artifacts...'
                deleteDir() // Deletes all files in the workspace
            }
        }
    }
}


def publishChecks(name, status, conclusion, summary, text=null) {
    // Publish checks to GitHub
    githubNotify(
        context: name,
        status: status,
        conclusion: conclusion,
        summary: summary,
        description: text
    )
}