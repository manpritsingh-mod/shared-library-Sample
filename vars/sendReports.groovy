// Main report generation function - calls specific report methods
def generateReport(String reportType = 'all', Map config = [:]) {
    core_utils.logInfo("Starting report generation...")
    
    try {
        // Call report generation logic
        return task_reportGeneration(reportType, config)
    } catch (Exception e) {
        core_utils.logError("Report generation failed: ${e.getMessage()}")
        return false
    }
}

// Report generation logic - main logic separated for easy changes
def task_reportGeneration(String reportType, Map config) {
    core_utils.logInfo("Report generation logic execution...")
    
    def reportResults = [
        testReportGenerated: false,
        coverageReportGenerated: false,
        buildReportGenerated: false,
        overallSuccess: false
    ]
    
    try {
        switch(reportType.toLowerCase()) {
            case 'test':
            case 'tests':
                reportResults.testReportGenerated = generateTestReport(config)
                break
            case 'coverage':
                reportResults.coverageReportGenerated = generateCoverageReport(config)
                break
            case 'build':
                reportResults.buildReportGenerated = generateBuildReport(config)
                break
            case 'all':
            default:
                reportResults.testReportGenerated = generateTestReport(config)
                reportResults.coverageReportGenerated = generateCoverageReport(config)
                reportResults.buildReportGenerated = generateBuildReport(config)
                break
        }
        
        reportResults.overallSuccess = (reportType == 'all') ? 
            (reportResults.testReportGenerated && reportResults.coverageReportGenerated && reportResults.buildReportGenerated) :
            (reportResults.testReportGenerated || reportResults.coverageReportGenerated || reportResults.buildReportGenerated)
        
        if (reportResults.overallSuccess) {
            core_utils.logInfo("Report generation completed successfully")
        } else {
            core_utils.logError("Report generation failed")
        }
        
        return reportResults
    } catch (Exception e) {
        core_utils.logError("Report generation logic failed: ${e.getMessage()}")
        reportResults.overallSuccess = false
        return reportResults
    }
}

// Test report generation function - calls test report logic
def generateTestReport(Map config = [:]) {
    core_utils.logInfo("Generating test reports...")
    
    try {
        // Call test report logic
        return task_testReportGeneration(config)
    } catch (Exception e) {
        core_utils.logError("Test report generation failed: ${e.getMessage()}")
        return false
    }
}

// Test report generation logic - main logic separated
def task_testReportGeneration(Map config) {
    core_utils.logInfo("Test report generation logic execution...")
    
    def language = config.project_language
    
    try {
        switch(language) {
            case 'java-maven':
                return generateJavaMavenTestReport(config)
            case 'java-gradle':
                return generateJavaGradleTestReport(config)
            case 'python':
                return generatePythonTestReport(config)
            default:
                core_utils.logWarning("No specific test report generation for language: ${language}")
                return true
        }
    } catch (Exception e) {
        core_utils.logError("Test report generation logic failed: ${e.getMessage()}")
        return false
    }
}

// Java Maven test report generation
def generateJavaMavenTestReport(Map config) {
    core_utils.logInfo("Generating Java Maven test reports...")
    
    try {
        // Publish test results using Jenkins plugins
        if (fileExists('target/surefire-reports/*.xml')) {
            publishTestResults([
                testResultsPattern: 'target/surefire-reports/*.xml'
            ])
            core_utils.logInfo("Maven test reports published successfully")
            return true
        } else {
            core_utils.logWarning("No Maven test reports found")
            return true
        }
    } catch (Exception e) {
        core_utils.logError("Maven test report generation failed: ${e.getMessage()}")
        return false
    }
}

// Java Gradle test report generation
def generateJavaGradleTestReport(Map config) {
    core_utils.logInfo("Generating Java Gradle test reports...")
    
    try {
        // Publish test results using Jenkins plugins
        if (fileExists('build/test-results/test/*.xml')) {
            publishTestResults([
                testResultsPattern: 'build/test-results/test/*.xml'
            ])
            core_utils.logInfo("Gradle test reports published successfully")
            return true
        } else {
            core_utils.logWarning("No Gradle test reports found")
            return true
        }
    } catch (Exception e) {
        core_utils.logError("Gradle test report generation failed: ${e.getMessage()}")
        return false
    }
}

// Python test report generation
def generatePythonTestReport(Map config) {
    core_utils.logInfo("Generating Python test reports...")
    
    try {
        // Publish test results using Jenkins plugins
        if (fileExists('test-results.xml') || fileExists('pytest.xml')) {
            def pattern = fileExists('test-results.xml') ? 'test-results.xml' : 'pytest.xml'
            publishTestResults([
                testResultsPattern: pattern
            ])
            core_utils.logInfo("Python test reports published successfully")
            return true
        } else {
            core_utils.logWarning("No Python test reports found")
            return true
        }
    } catch (Exception e) {
        core_utils.logError("Python test report generation failed: ${e.getMessage()}")
        return false
    }
}

// Coverage report generation function - calls coverage report logic
def generateCoverageReport(Map config = [:]) {
    core_utils.logInfo("Generating coverage reports...")
    
    try {
        // Call coverage report logic
        return task_coverageReportGeneration(config)
    } catch (Exception e) {
        core_utils.logError("Coverage report generation failed: ${e.getMessage()}")
        return false
    }
}

// Coverage report generation logic - main logic separated
def task_coverageReportGeneration(Map config) {
    core_utils.logInfo("Coverage report generation logic execution...")
    
    def language = config.project_language
    
    try {
        switch(language) {
            case 'java-maven':
                return generateJavaMavenCoverageReport(config)
            case 'java-gradle':
                return generateJavaGradleCoverageReport(config)
            case 'python':
                return generatePythonCoverageReport(config)
            default:
                core_utils.logWarning("No specific coverage report generation for language: ${language}")
                return true
        }
    } catch (Exception e) {
        core_utils.logError("Coverage report generation logic failed: ${e.getMessage()}")
        return false
    }
}

// Java Maven coverage report generation
def generateJavaMavenCoverageReport(Map config) {
    core_utils.logInfo("Generating Java Maven coverage reports...")
    
    try {
        // Publish coverage results using Jenkins plugins
        if (fileExists('target/site/jacoco/jacoco.xml')) {
            publishCoverage([
                adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')],
                sourceFileResolver: sourceFiles('STORE_ALL_BUILD')
            ])
            core_utils.logInfo("Maven coverage reports published successfully")
            return true
        } else {
            core_utils.logWarning("No Maven coverage reports found")
            return true
        }
    } catch (Exception e) {
        core_utils.logError("Maven coverage report generation failed: ${e.getMessage()}")
        return false
    }
}

// Java Gradle coverage report generation
def generateJavaGradleCoverageReport(Map config) {
    core_utils.logInfo("Generating Java Gradle coverage reports...")
    
    try {
        // Publish coverage results using Jenkins plugins
        if (fileExists('build/reports/jacoco/test/jacocoTestReport.xml')) {
            publishCoverage([
                adapters: [jacocoAdapter('build/reports/jacoco/test/jacocoTestReport.xml')],
                sourceFileResolver: sourceFiles('STORE_ALL_BUILD')
            ])
            core_utils.logInfo("Gradle coverage reports published successfully")
            return true
        } else {
            core_utils.logWarning("No Gradle coverage reports found")
            return true
        }
    } catch (Exception e) {
        core_utils.logError("Gradle coverage report generation failed: ${e.getMessage()}")
        return false
    }
}

// Python coverage report generation
def generatePythonCoverageReport(Map config) {
    core_utils.logInfo("Generating Python coverage reports...")
    
    try {
        // Publish coverage results using Jenkins plugins
        if (fileExists('coverage.xml')) {
            publishCoverage([
                adapters: [coberturaAdapter('coverage.xml')],
                sourceFileResolver: sourceFiles('STORE_ALL_BUILD')
            ])
            core_utils.logInfo("Python coverage reports published successfully")
            return true
        } else {
            core_utils.logWarning("No Python coverage reports found")
            return true
        }
    } catch (Exception e) {
        core_utils.logError("Python coverage report generation failed: ${e.getMessage()}")
        return false
    }
}

// Build report generation function - calls build report logic
def generateBuildReport(Map config = [:]) {
    core_utils.logInfo("Generating build reports...")
    
    try {
        // Call build report logic
        return task_buildReportGeneration(config)
    } catch (Exception e) {
        core_utils.logError("Build report generation failed: ${e.getMessage()}")
        return false
    }
}

// Build report generation logic - main logic separated
def task_buildReportGeneration(Map config) {
    core_utils.logInfo("Build report generation logic execution...")
    
    try {
        // Archive build artifacts
        if (fileExists('target/') || fileExists('build/') || fileExists('dist/')) {
            archiveBuildArtifacts(config)
        }
        
        // Generate build summary
        generateBuildSummary(config)
        
        core_utils.logInfo("Build reports generated successfully")
        return true
    } catch (Exception e) {
        core_utils.logError("Build report generation logic failed: ${e.getMessage()}")
        return false
    }
}

// Archive build artifacts
def archiveBuildArtifacts(Map config) {
    def language = config.project_language
    def artifacts = []
    
    switch(language) {
        case 'java-maven':
            if (fileExists('target/*.jar') || fileExists('target/*.war')) {
                artifacts.add('target/*.jar')
                artifacts.add('target/*.war')
            }
            break
        case 'java-gradle':
            if (fileExists('build/libs/*.jar') || fileExists('build/libs/*.war')) {
                artifacts.add('build/libs/*.jar')
                artifacts.add('build/libs/*.war')
            }
            break
        case 'python':
            if (fileExists('dist/*.whl') || fileExists('dist/*.tar.gz')) {
                artifacts.add('dist/*.whl')
                artifacts.add('dist/*.tar.gz')
            }
            break
    }
    
    if (artifacts) {
        archiveArtifacts artifacts: artifacts.join(','), allowEmptyArchive: true
        core_utils.logInfo("Artifacts archived: ${artifacts}")
    }
}

// Generate build summary
def generateBuildSummary(Map config) {
    def buildInfo = [
        project: env.JOB_NAME,
        buildNumber: env.BUILD_NUMBER,
        language: config.project_language,
        branch: env.BRANCH_NAME ?: env.GIT_BRANCH,
        timestamp: new Date().format("yyyy-MM-dd HH:mm:ss"),
        duration: currentBuild.duration ? "${Math.round(currentBuild.duration / 1000)}s" : "N/A"
    ]
    
    def summaryContent = """
Build Summary Report
====================
Project: ${buildInfo.project}
Build Number: #${buildInfo.buildNumber}
Language: ${buildInfo.language}
Branch: ${buildInfo.branch}
Timestamp: ${buildInfo.timestamp}
Duration: ${buildInfo.duration}
Status: ${currentBuild.currentResult ?: 'IN_PROGRESS'}
"""
    
    writeFile file: 'build-summary.txt', text: summaryContent
    archiveArtifacts artifacts: 'build-summary.txt', allowEmptyArchive: true
    core_utils.logInfo("Build summary generated")
}

// Main send report function - calls specific send methods
def sendReport(String reportType = 'all', Map config = [:]) {
    core_utils.logInfo("Sending reports...")
    
    try {
        // Call send report logic
        return task_sendReports(reportType, config)
    } catch (Exception e) {
        core_utils.logError("Send report failed: ${e.getMessage()}")
        return false
    }
}

// Send report logic - main logic separated
def task_sendReports(String reportType, Map config) {
    core_utils.logInfo("Send report logic execution...")
    
    try {
        // Generate reports first if not already generated
        def reportResults = generateReport(reportType, config)
        
        if (!reportResults.overallSuccess) {
            core_utils.logWarning("Some reports failed to generate, continuing with available reports")
        }
        
        // Send notification with report links
        def reportMessage = generateReportMessage(reportResults, config)
        
        // Send via notifications
        notify.notifyAll('success', reportMessage, [
            enableSlack: config.enableSlackReports != false,
            enableEmail: config.enableEmailReports != false
        ])
        
        core_utils.logInfo("Reports sent successfully")
        return true
    } catch (Exception e) {
        core_utils.logError("Send report logic failed: ${e.getMessage()}")
        return false
    }
}

// Generate report message for notifications
def generateReportMessage(Map reportResults, Map config) {
    def buildUrl = env.BUILD_URL ?: 'N/A'
    
    def message = "📊 Build Reports Generated\n\n"
    
    if (reportResults.testReportGenerated) {
        message += "✅ Test Reports: ${buildUrl}testReport/\n"
    }
    if (reportResults.coverageReportGenerated) {
        message += "✅ Coverage Reports: ${buildUrl}coverage/\n"
    }
    if (reportResults.buildReportGenerated) {
        message += "✅ Build Artifacts: ${buildUrl}artifact/\n"
    }
    
    message += "\n🔗 Full Build Details: ${buildUrl}"
    
    return message
}

// Helper function to publish test results
def publishTestResults(Map options) {
    publishTestResults testResultsPattern: options.testResultsPattern
}

// Task-specific report stage for templates
def task_reportStage(Map config = [:]) {
    core_utils.logInfo("Report stage execution...")
    
    try {
        // Generate all reports
        def reportResults = generateReport('all', config)
        
        // Send reports if configured
        if (config.sendReports != false) {
            sendReport('all', config)
        }
        
        return reportResults.overallSuccess
    } catch (Exception e) {
        core_utils.logError("Report stage failed: ${e.getMessage()}")
        return false
    }
}
