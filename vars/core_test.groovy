// Main test function - calls other test methods based on configuration
def runTest(Map config = [:]) {
    core_utils.logInfo("Starting test execution...")
    
    def testResults = [
        lintPassed: true,
        unitTestsPassed: true,
        overallPassed: true
    ]
    
    try {
        // Step 1: Run lint tests if enabled
        if (core_utils.shouldExecuteStage('lint', config)) {
            core_utils.logInfo("Lint tests are enabled - executing...")
            testResults.lintPassed = runLint(config)
        } else {
            core_utils.logInfo("Lint tests are disabled - skipping...")
        }
        
        // Step 2: Run unit tests if enabled
        if (core_utils.shouldExecuteStage('unittest', config)) {
            core_utils.logInfo("Unit tests are enabled - executing...")
            testResults.unitTestsPassed = runUnitTest(config)
        } else {
            core_utils.logInfo("Unit tests are disabled - skipping...")
        }
        
        // Overall result
        testResults.overallPassed = testResults.lintPassed && testResults.unitTestsPassed
        
        if (testResults.overallPassed) {
            core_utils.logInfo("All tests completed successfully")
        } else {
            core_utils.logError("Some tests failed")
        }
        
        return testResults
    } catch (Exception e) {
        core_utils.logError("Test execution failed: ${e.getMessage()}")
        testResults.overallPassed = false
        return testResults
    }
}

// Lint test function - calls lint logic method
def runLint(Map config = [:]) {
    core_utils.logInfo("Executing lint tests...")
    
    try {
        // Call lint logic method
        return task_lintStage(config)
    } catch (Exception e) {
        core_utils.logError("Lint test execution failed: ${e.getMessage()}")
        return false
    }
}

// Lint test logic - main logic separated for easy changes
def task_lintStage(Map config = [:]) {
    core_utils.logInfo("Lint stage logic execution...")
    
    def language = config.project_language
    def lintTool = getLintTool(language, config)
    
    core_utils.logInfo("Running lint tests for ${language} using ${lintTool}")
    
    switch(language) {
        case 'java-maven':
        case 'java-gradle':
            return runJavaLint(language, lintTool, config)
        case 'python':
            return runPythonLint(lintTool, config)
        default:
            core_utils.logError("Unsupported language for lint tests: ${language}")
            return false
    }
}

// Java lint function - calls Java lint logic
def runJavaLint(String language, String lintTool, Map config = [:]) {
    core_utils.logInfo("Running Java lint with ${lintTool}")
    
    try {
        return task_javaLintLogic(language, lintTool, config)
    } catch (Exception e) {
        core_utils.logError("Java lint failed: ${e.getMessage()}")
        return false
    }
}

// Java lint logic - main logic separated
def task_javaLintLogic(String language, String lintTool, Map config = [:]) {
    core_utils.logInfo("Java lint logic execution...")
    
    def command = ""
    
    switch(lintTool) {
        case 'checkstyle':
            if (language == 'java-maven') {
                command = 'mvn checkstyle:check -B'
            } else if (language == 'java-gradle') {
                def gradleCommand = fileExists('gradlew') ? './gradlew' : 'gradle'
                command = "${gradleCommand} checkstyleMain --no-daemon"
            }
            break
        case 'spotbugs':
            if (language == 'java-maven') {
                command = 'mvn spotbugs:check -B'
            } else if (language == 'java-gradle') {
                def gradleCommand = fileExists('gradlew') ? './gradlew' : 'gradle'
                command = "${gradleCommand} spotbugsMain --no-daemon"
            }
            break
        case 'pmd':
            if (language == 'java-maven') {
                command = 'mvn pmd:check -B'
            } else if (language == 'java-gradle') {
                def gradleCommand = fileExists('gradlew') ? './gradlew' : 'gradle'
                command = "${gradleCommand} pmdMain --no-daemon"
            }
            break
        default:
            core_utils.logError("Unsupported Java lint tool: ${lintTool}")
            return false
    }
    
    core_utils.logInfo("Running: ${command}")
    
    if (isUnix()) {
        sh script: command
    } else {
        bat script: command
    }
    
    core_utils.logInfo("Java lint completed successfully")
    return true
}

// Python lint function - calls Python lint logic
def runPythonLint(String lintTool, Map config = [:]) {
    core_utils.logInfo("Running Python lint with ${lintTool}")
    
    try {
        return task_pythonLintLogic(lintTool, config)
    } catch (Exception e) {
        core_utils.logError("Python lint failed: ${e.getMessage()}")
        return false
    }
}

// Python lint logic - main logic separated
def task_pythonLintLogic(String lintTool, Map config = [:]) {
    core_utils.logInfo("Python lint logic execution...")
    
    def command = ""
    
    switch(lintTool) {
        case 'pylint':
            command = 'pylint **/*.py --output-format=text'
            break
        case 'flake8':
            command = 'flake8 .'
            break
        case 'black':
            command = 'black --check .'
            break
        default:
            core_utils.logError("Unsupported Python lint tool: ${lintTool}")
            return false
    }
    
    core_utils.logInfo("Running: ${command}")
    
    if (isUnix()) {
        sh script: command
    } else {
        bat script: command
    }
    
    core_utils.logInfo("Python lint completed successfully")
    return true
}

// Unit test function - calls unit test logic method
def runUnitTest(Map config = [:]) {
    core_utils.logInfo("Executing unit tests...")
    
    try {
        // Call unit test logic method
        return task_unitTestStage(config)
    } catch (Exception e) {
        core_utils.logError("Unit test execution failed: ${e.getMessage()}")
        return false
    }
}

// Unit test logic - main logic separated for easy changes
def task_unitTestStage(Map config = [:]) {
    core_utils.logInfo("Unit test stage logic execution...")
    
    def language = config.project_language
    def testTool = getUnitTestTool(language, config)
    
    core_utils.logInfo("Running unit tests for ${language} using ${testTool}")
    
    switch(language) {
        case 'java-maven':
        case 'java-gradle':
            return runJavaUnitTest(language, testTool, config)
        case 'python':
            return runPythonUnitTest(testTool, config)
        default:
            core_utils.logError("Unsupported language for unit tests: ${language}")
            return false
    }
}

// Java unit test function - calls Java unit test logic
def runJavaUnitTest(String language, String testTool, Map config = [:]) {
    core_utils.logInfo("Running Java unit tests with ${testTool}")
    
    try {
        return task_javaUnitTestLogic(language, testTool, config)
    } catch (Exception e) {
        core_utils.logError("Java unit test failed: ${e.getMessage()}")
        return false
    }
}

// Java unit test logic - main logic separated
def task_javaUnitTestLogic(String language, String testTool, Map config = [:]) {
    core_utils.logInfo("Java unit test logic execution...")
    
    def command = ""
    
    switch(testTool) {
        case 'junit':
            if (language == 'java-maven') {
                command = 'mvn test -B'
            } else if (language == 'java-gradle') {
                def gradleCommand = fileExists('gradlew') ? './gradlew' : 'gradle'
                command = "${gradleCommand} test --no-daemon"
            }
            break
        case 'testng':
            if (language == 'java-maven') {
                command = 'mvn test -B'
            } else if (language == 'java-gradle') {
                def gradleCommand = fileExists('gradlew') ? './gradlew' : 'gradle'
                command = "${gradleCommand} test --no-daemon"
            }
            break
        default:
            core_utils.logError("Unsupported Java test tool: ${testTool}")
            return false
    }
    
    core_utils.logInfo("Running: ${command}")
    
    if (isUnix()) {
        sh script: command
    } else {
        bat script: command
    }
    
    core_utils.logInfo("Java unit tests completed successfully")
    return true
}

// Python unit test function - calls Python unit test logic
def runPythonUnitTest(String testTool, Map config = [:]) {
    core_utils.logInfo("Running Python unit tests with ${testTool}")
    
    try {
        return task_pythonUnitTestLogic(testTool, config)
    } catch (Exception e) {
        core_utils.logError("Python unit test failed: ${e.getMessage()}")
        return false
    }
}

// Python unit test logic - main logic separated
def task_pythonUnitTestLogic(String testTool, Map config = [:]) {
    core_utils.logInfo("Python unit test logic execution...")
    
    def command = ""
    
    switch(testTool) {
        case 'pytest':
            command = 'pytest --verbose --tb=short'
            break
        case 'unittest':
            command = 'python -m unittest discover -v'
            break
        default:
            core_utils.logError("Unsupported Python test tool: ${testTool}")
            return false
    }
    
    core_utils.logInfo("Running: ${command}")
    
    if (isUnix()) {
        sh script: command
    } else {
        bat script: command
    }
    
    core_utils.logInfo("Python unit tests completed successfully")
    return true
}

// Helper function to get lint tool from configuration
def getLintTool(String language, Map config = [:]) {
    def lintTool = null
    
    if (language == 'java-maven' || language == 'java-gradle') {
        lintTool = config.tool_for_lint_testing?.java ?: 'checkstyle'
    } else if (language == 'python') {
        lintTool = config.tool_for_lint_testing?.python ?: 'pylint'
    }
    
    core_utils.logInfo("Selected lint tool: ${lintTool}")
    return lintTool
}

// Helper function to get unit test tool from configuration
def getUnitTestTool(String language, Map config = [:]) {
    def testTool = null
    
    if (language == 'java-maven' || language == 'java-gradle') {
        testTool = config.tool_for_unit_testing?.java ?: 'junit'
    } else if (language == 'python') {
        testTool = config.tool_for_unit_testing?.python ?: 'pytest'
    }
    
    core_utils.logInfo("Selected unit test tool: ${testTool}")
    return testTool
}
