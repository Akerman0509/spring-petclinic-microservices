pipeline {
    agent any
    
    parameters {
        // Parameters to specify which branch to deploy for each service
        string(name: 'CUSTOMERS_SERVICE', defaultValue: 'main', description: 'Branch for customers-service')
        string(name: 'VETS_SERVICE', defaultValue: 'main', description: 'Branch for vets-service')
        string(name: 'VISITS_SERVICE', defaultValue: 'main', description: 'Branch for visits-service')
        string(name: 'API_GATEWAY', defaultValue: 'main', description: 'Branch for api-gateway')
        string(name: 'CONFIG_SERVER', defaultValue: 'main', description: 'Branch for config-server')
        string(name: 'DISCOVERY_SERVER', defaultValue: 'main', description: 'Branch for discovery-server')
        string(name: 'ADMIN_SERVER', defaultValue: 'main', description: 'Branch for admin-server')
        
        // Option to clean up developer environment
        booleanParam(name: 'CLEANUP', defaultValue: false, description: 'Clean up developer environment')
    }
    
    environment {
        // Docker Hub credentials
        // DOCKER_HUB_CREDS = credentials('docker-hub-credentials')
        // Docker Hub username
        DOCKER_HUB_USERNAME = 'akerman0509'
        // Git repository URL
        GIT_REPO_URL = 'https://github.com/Akerman0509/spring-petclinic-microservices.git'
        // Services list
        SERVICES = 'customers-service,vets-service,visits-service,api-gateway,config-server,discovery-server,admin-server'
        // Kubernetes namespace for developer testing
        K8S_NAMESPACE = 'petclinic-dev'
    }

    stages {
        stage('Cleanup') {
            when {
                expression { params.CLEANUP == true }
            }
            steps {
                script {
                    echo "Cleaning up developer environment..."
                    sh "kubectl delete namespace ${env.K8S_NAMESPACE} --ignore-not-found=true"
                    sh "kubectl create namespace ${env.K8S_NAMESPACE}"
                }
            }
        }
        
        stage('Checkout & Prepare Services') {
            steps {
                script {
                    echo "Checking out the repository..."
                        }
                    }
                }

    }
}


