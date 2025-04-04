pipeline {
    agent { label 'ec2-agent' }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
    }
    tools {
        maven 'maven3'
        // jdk 'jdk17'
    }
    environment {
        WORKSPACE = "${env.WORKSPACE}"
        // List of services without test folders       
        SERVICES_WITHOUT_TESTS = "spring-petclinic-admin-server spring-petclinic-genai-service"
        // Full list of services
        ALL_SERVICES = "spring-petclinic-admin-server spring-petclinic-api-gateway spring-petclinic-config-server spring-petclinic-customers-service spring-petclinic-discovery-server spring-petclinic-genai-service spring-petclinic-vets-service spring-petclinic-visits-service"
    }
    stages {
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
        
        stage('Test Services') {
            when {
                expression { return env.CHANGED_SERVICES != "" }
            }
            steps {
                publishChecks name: 'Test Services', status: 'IN_PROGRESS', 
                    summary: 'Running tests for changed services'
                script {
                    def serviceList = env.CHANGED_SERVICES.trim().split(" ")
                    def testDetails = [:]
                    def testFailures = 0
                    def testPasses = 0
                    
                    // Prepare JaCoCo report inputs
                    def jacocoExecFiles = []
                    def jacocoClassDirs = []
                    def jacocoSrcDirs = []
                    
                    for (service in serviceList) {
                        echo "Testing service: ${service}"
                        dir(service) {
                            // Check if the service has tests
                            if (!env.SERVICES_WITHOUT_TESTS.contains(service)) {
                                try {
                                    def testOutput = sh(script: 'mvn clean test', returnStdout: true)

                                    // def testOutput = sh(script: 'mvn clean test -Djdk.attach.allowAttachSelf=true', returnStdout: true)

                                    testDetails[service] = [
                                        status: 'SUCCESS',
                                        output: testOutput
                                    ]
                                    testPasses++
                                } catch (Exception e) {
                                    echo "Warning: Tests failed for ${service}, but continuing pipeline"
                                    testDetails[service] = [
                                        status: 'FAILURE',
                                        error: e.getMessage()
                                    ]
                                    testFailures++
                                    currentBuild.result = 'UNSTABLE'
                                } finally {
                                    // Publish test results regardless of test success/failure
                                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                                    
                                    // Collect JaCoCo data for aggregation (if it exists)
                                    if (fileExists('target/jacoco.exec')) {
                                        jacocoExecFiles.add("${service}/target/jacoco.exec")
                                        jacocoClassDirs.add("${service}/target/classes")
                                        jacocoSrcDirs.add("${service}/src/main/java")
                                    }
                                }
                            } else {
                                echo "Skipping tests for ${service} as it does not have test folders"
                                testDetails[service] = [
                                    status: 'SKIPPED',
                                    reason: 'No test folders'
                                ]
                            }
                        }
                    }
                    
                    // Generate a single aggregated JaCoCo report outside the loop
                    if (jacocoExecFiles.size() > 0) {
                        echo "Generating aggregated JaCoCo report for ${jacocoExecFiles.size()} services"
                        jacoco(
                            execPattern: jacocoExecFiles.join(','),
                            classPattern: jacocoClassDirs.join(','),
                            sourcePattern: jacocoSrcDirs.join(','),
                            exclusionPattern: '**/src/test*',
                            outputDirectory: 'target/jacoco-reports',
                            reportTitle: 'JaCoCo Aggregated Report - All Services',
                            skipCopyOfSrcFiles: true,
                            dumpOnExit: true,
                            minimumLineCoverage: '70',
                            changeBuildStatus: true
                        )
                        
                        // Read the JaCoCo XML report to check coverage
                        def jacocoReportPath = "${env.WORKSPACE}/target/jacoco-reports/jacoco.xml"
                        if (fileExists(jacocoReportPath)) {
                            def jacocoReport = readFile(jacocoReportPath)
                            def coverageXml = new XmlSlurper().parseText(jacocoReport)
                            def lineCoverage = coverageXml.counter.find { it.@type == 'LINE' }
                            def covered = lineCoverage ? lineCoverage.@covered.toFloat() : 0
                            def missed = lineCoverage ? lineCoverage.@missed.toFloat() : 0
                            def totalLines = covered + missed
                            def coveragePercent = totalLines > 0 ? (covered / totalLines) * 100 : 0
                            echo "+++ coveragePercent = ${coveragePercent}"
                            def coverageFormatted = String.format("%.2f", coveragePercent)
                            
                            def coverageCheckResult = coveragePercent >= 70 ? 'SUCCESS' : 'NEUTRAL'
                            def coverageCheckMessage = coveragePercent >= 70 ? 
                                "✅ Line coverage ${coverageFormatted}% meets the requirement of 70%" :
                                "⚠️ Line coverage ${coverageFormatted}% is below the required 70%"
                            
                            // Publish coverage check results
                            publishChecks name: 'Code Coverage', status: 'COMPLETED', 
                                conclusion: coverageCheckResult,
                                summary: "Line coverage: ${coverageFormatted}%",
                                text: """## JaCoCo Coverage Report
                                    
                                    ${coverageCheckMessage}
                                    
                                    | Metric | Covered | Missed | Total | Coverage |
                                    |--------|---------|--------|-------|----------|
                                    | Lines | ${covered.toInteger()} | ${missed.toInteger()} | ${totalLines.toInteger()} | ${coverageFormatted}% |
                                    
                                    See the detailed JaCoCo report in Jenkins for more information."""
                            
                            // Update build status if coverage is below threshold
                            if (coveragePercent < 70) {
                                currentBuild.result = currentBuild.result == 'UNSTABLE' ? 'UNSTABLE' : 'UNSTABLE'
                            }
                        } else {
                            echo "Warning: JaCoCo XML report not found at ${jacocoReportPath}"
                            publishChecks name: 'Code Coverage', status: 'COMPLETED', 
                                conclusion: 'NEUTRAL',
                                summary: "Could not determine coverage",
                                text: "JaCoCo XML report not found. Coverage check could not be performed."
                        }
                    }
                    
                    // Store test details for report
                    env.TEST_SUMMARY = "Tests passed: ${testPasses}, failed: ${testFailures}, skipped: ${serviceList.size() - testPasses - testFailures}"
                    
                    // Create detailed test report text
                    def testReportText = "# Test Results Summary\n\n"
                    testReportText += "| Service | Status | Details |\n"
                    testReportText += "|---------|--------|--------|\n"
                    
                    for (service in serviceList) {
                        def details = testDetails[service]
                        def statusEmoji = details.status == 'SUCCESS' ? '✅' : details.status == 'SKIPPED' ? '⏭️' : '❌' 
                        def detailText = details.status == 'SUCCESS' ? 'Tests passed' : 
                                        details.status == 'SKIPPED' ? details.reason : 'Tests failed'
                        testReportText += "| ${service} | ${statusEmoji} ${details.status} | ${detailText} |\n"
                    }
                    
                    testReportText += "\n\n## JUnit Results\nSee Jenkins test results for detailed JUnit information.\n\n"
                    testReportText += "## JaCoCo Coverage\nSee Jenkins coverage reports for aggregated code coverage information."
                    
                    env.TEST_REPORT = testReportText
                }
                publishChecks name: 'Test Services', status: 'COMPLETED', 
                    conclusion: currentBuild.result == 'UNSTABLE' ? 'NEUTRAL' : 'SUCCESS',
                    summary: env.TEST_SUMMARY,
                    text: env.TEST_REPORT
            }
            post {
                failure {
                    publishChecks name: 'Test Services', status: 'COMPLETED', conclusion: 'FAILURE', 
                        summary: 'Test execution failed'
                }
            }
        }
        
        // stage('Build Services') {
        //     when {
        //         expression { return env.CHANGED_SERVICES != "" }
        //     }
        //     steps {
        //         publishChecks name: 'Build Services', status: 'IN_PROGRESS',
        //             summary: 'Building changed services'
        //         script {
        //             def serviceList = env.CHANGED_SERVICES.trim().split(" ")
        //             def buildDetails = []
                    
        //             for (service in serviceList) {
        //                 echo "Building service: ${service}"
        //                 dir(service) {
        //                     try {
        //                         sh 'mvn package -DskipTests'
        //                         def artifactName = sh(script: 'find target -name "*.jar" | head -1', returnStdout: true).trim()
        //                         buildDetails.add("✅ ${service}: Successfully built ${artifactName}")
        //                         archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        //                     } catch (Exception e) {
        //                         buildDetails.add("❌ ${service}: Build failed - ${e.getMessage()}")
        //                         currentBuild.result = 'UNSTABLE'
        //                     }
        //                 }
        //             }
                    
        //             // Create build report
        //             env.BUILD_DETAILS = buildDetails.join('\n')
        //         }
        //         publishChecks name: 'Build Services', status: 'COMPLETED', 
        //             conclusion: currentBuild.result == 'UNSTABLE' ? 'NEUTRAL' : 'SUCCESS',
        //             summary: "Built ${env.CHANGED_SERVICES.trim().split(' ').size()} services",
        //             text: """## Build Results
        //                   ```
        //                   ${env.BUILD_DETAILS}
        //                   ```
                          
        //                   All artifacts have been archived."""
        //     }
        //     post {
        //         failure {
        //             publishChecks name: 'Build Services', status: 'COMPLETED', conclusion: 'FAILURE',
        //                 summary: 'Build process failed'
        //         }
        //     }
        // }

    }
    post {
        always {
            cleanWs()
        }
    }
}