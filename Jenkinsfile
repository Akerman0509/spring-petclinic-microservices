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
        // Add more services as needed
        
        // Option to clean up developer environment
        booleanParam(name: 'CLEANUP', defaultValue: false, description: 'Clean up developer environment')
    }
    
    environment {
        // Kubernetes config
        // KUBECONFIG = credentials('kubeconfig')
        // Docker Hub username
        DOCKER_HUB_USERNAME = 'akerman0509'
        // Base domain for developer testing
        // BASE_DOMAIN = 'petclinic.local'
        // Git repository URL
        GIT_REPO_URL = 'https://github.com/Akerman0509/spring-petclinic-microservices.git'
        // Services list
        SERVICES = 'customers-service,vets-service,visits-service,api-gateway,config-server,discovery-server,admin-server'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    // Clean workspace
                    deleteDir()
                    
                    // Checkout all branches
                    def serviceList = env.SERVICES.split(',')
                    for (service in serviceList) {
                        def branchParam = service.toUpperCase().replaceAll('-', '_')
                        def branch = params[branchParam]
                    echo "== WORKING IN branch: ${branch}"
                        
                        dir(service) {
                            checkout([
                                $class: 'GitSCM',
                                branches: [[name: "*/${branch}"]],
                                doGenerateSubmoduleConfigurations: false,
                                extensions: [],
                                submoduleCfg: [],
                                userRemoteConfigs: [[
                                    url: "${GIT_REPO_URL}"
                                ]]
                            ])
                        }
                    }
                }
            }
        }
        
        // stage('Build and Push Docker Images') {
        //     steps {
        //         script {
        //             def serviceList = env.SERVICES.split(',')
        //             for (service in serviceList) {
        //                 def branchParam = service.toUpperCase().replaceAll('-', '_')
        //                 def branch = params[branchParam]
                        
        //                 dir(service) {
        //                     // Get the latest commit ID for the branch
        //                     def commitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                            
        //                     echo "Building ${service} from branch ${branch} with commit ID ${commitId}"
                            
        //                     // Build Docker image with commit ID as tag
        //                     sh "docker build -t ${DOCKER_HUB_USERNAME}/${service}:${commitId} ."
                            
        //                     // Push image to Docker Hub
        //                     sh "docker push ${DOCKER_HUB_USERNAME}/${service}:${commitId}"
                            
        //                     // If this is the main branch, also tag as latest
        //                     if (branch == 'main') {
        //                         sh "docker tag ${DOCKER_HUB_USERNAME}/${service}:${commitId} ${DOCKER_HUB_USERNAME}/${service}:latest"
        //                         sh "docker push ${DOCKER_HUB_USERNAME}/${service}:latest"
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }
        
//         stage('Deploy to Kubernetes') {
//             when {
//                 expression { !params.CLEANUP }
//             }
//             steps {
//                 script {
//                     // Make sure kubectl uses the correct config
//                     sh "mkdir -p \$HOME/.kube"
//                     sh "cp ${KUBECONFIG} \$HOME/.kube/config"
                    
//                     // Create or ensure namespace exists
//                     sh "kubectl create namespace petclinic-dev --dry-run=client -o yaml | kubectl apply -f -"
                    
//                     def serviceList = env.SERVICES.split(',')
//                     for (service in serviceList) {
//                         def branchParam = service.toUpperCase().replaceAll('-', '_')
//                         def branch = params[branchParam]
                        
//                         dir(service) {
//                             def commitId = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
//                             def imageTag = commitId
                            
//                             // If this is main branch and not a developer build, use 'latest' tag
//                             if (branch == 'main') {
//                                 def isDeveloperBuild = false
//                                 for (param in params) {
//                                     if (param.key.endsWith('_SERVICE') && param.value != 'main') {
//                                         isDeveloperBuild = true
//                                         break
//                                     }
//                                 }
                                
//                                 if (!isDeveloperBuild) {
//                                     imageTag = 'latest'
//                                 }
//                             }
                            
//                             // Generate deployment YAML
//                             writeFile file: "${service}-deployment.yaml", text: """
// apiVersion: apps/v1
// kind: Deployment
// metadata:
//   name: ${service}
//   namespace: petclinic-dev
// spec:
//   replicas: 1
//   selector:
//     matchLabels:
//       app: ${service}
//   template:
//     metadata:
//       labels:
//         app: ${service}
//     spec:
//       containers:
//       - name: ${service}
//         image: ${DOCKER_HUB_USERNAME}/${service}:${imageTag}
//         ports:
//         - containerPort: 8080
// ---
// apiVersion: v1
// kind: Service
// metadata:
//   name: ${service}
//   namespace: petclinic-dev
// spec:
//   type: NodePort
//   ports:
//   - port: 8080
//     targetPort: 8080
//   selector:
//     app: ${service}
// """
                            
//                             // Apply deployment
//                             sh "kubectl apply -f ${service}-deployment.yaml"
//                         }
//                     }
                    
//                     // Create Ingress for developer testing
//                     writeFile file: "ingress.yaml", text: """
// apiVersion: networking.k8s.io/v1
// kind: Ingress
// metadata:
//   name: petclinic-ingress
//   namespace: petclinic-dev
//   annotations:
//     kubernetes.io/ingress.class: nginx
// spec:
//   rules:
//   - host: ${BASE_DOMAIN}
//     http:
//       paths:
//       - path: /
//         pathType: Prefix
//         backend:
//           service:
//             name: api-gateway
//             port:
//               number: 8080
// """
                    
//                     sh "kubectl apply -f ingress.yaml"
                    
//                     // Get NodePort for each service
//                     def serviceNodePorts = [:]
//                     for (service in serviceList) {
//                         def nodePort = sh(
//                             script: "kubectl get svc ${service} -n petclinic-dev -o jsonpath='{.spec.ports[0].nodePort}'",
//                             returnStdout: true
//                         ).trim()
//                         serviceNodePorts[service] = nodePort
//                     }
                    
//                     // Print access information
//                     echo "================== Access Information =================="
//                     echo "Add the following to your /etc/hosts file:"
//                     echo "<worker-node-ip> ${BASE_DOMAIN}"
//                     echo ""
//                     echo "Service endpoints:"
//                     for (service in serviceList) {
//                         echo "${service}: http://${BASE_DOMAIN}:${serviceNodePorts[service]}"
//                     }
//                     echo "====================================================="
//                 }
//             }
//         }
        
//         stage('Cleanup Developer Environment') {
//             when {
//                 expression { params.CLEANUP }
//             }
//             steps {
//                 script {
//                     // Make sure kubectl uses the correct config
//                     sh "mkdir -p \$HOME/.kube"
//                     sh "cp ${KUBECONFIG} \$HOME/.kube/config"
                    
//                     // Delete all resources in the namespace
//                     sh "kubectl delete namespace petclinic-dev"
                    
//                     echo "Developer environment has been cleaned up"
//                 }
//             }
//         }
    }
    
    post {
        always {
            // Clean workspace
            cleanWs()
        }
        
        success {
            echo "Pipeline completed successfully!"
            
            // Create a link to the cleanup job
            echo """
<h2>Developer Environment Information</h2>
<p>To clean up this deployment, click <a href="${env.JENKINS_URL}job/cleanup_developer_build/build?delay=0sec">here</a></p>
"""
        }
        
        failure {
            echo "Pipeline failed!"
        }
    }
}