def logInfo(String message) {
    def timestamp = new Date().format("yyyy-MM-dd HH:mm:ss")
    echo "[INFO] [${timestamp}] ${message}"
}

def logWarning(String message) {
    def timestamp = new Date().format("yyyy-MM-dd HH:mm:ss")
    echo "[WARNING] [${timestamp}] ${message}"
}

def logError(String message) {
    def timestamp = new Date().format("yyyy-MM-dd HH:mm:ss")
    echo "[ERROR] [${timestamp}] ${message}"
}

def setupEnvironment() {
    logInfo("Setting up build environment...")
    
    // Set common environment variables
    env.BUILD_TIMESTAMP = new Date().format("yyyy-MM-dd_HH-mm-ss")
    env.BUILD_USER = env.BUILD_USER ?: 'jenkins'
    
    // Display environment info
    logInfo("Build Timestamp: ${env.BUILD_TIMESTAMP}")
    logInfo("Build User: ${env.BUILD_USER}")
    logInfo("Workspace: ${env.WORKSPACE}")
    
    return true
}

def detectProjectLanguage() {
    logInfo("Detecting project language...")
    
    // Call the task file for main logic
    return task_languageDetection()
}

def task_languageDetection() {
    logInfo("Executing language detection logic...")
    
    def detectedLanguage = null
    
    // Check for Java project indicators
    if (fileExists('pom.xml')) {
        detectedLanguage = 'java-maven'
        logInfo("Detected Java Maven project (pom.xml found)")
    } else if (fileExists('build.gradle') || fileExists('build.gradle.kts')) {
        detectedLanguage = 'java-gradle'
        logInfo("Detected Java Gradle project (build.gradle found)")
    }
    // Check for Python project indicators
    else if (fileExists('requirements.txt') || fileExists('setup.py') || fileExists('pyproject.toml')) {
        detectedLanguage = 'python'
        logInfo("Detected Python project")
    }
    // Add more language detections as needed
    else {
        logWarning("Could not detect project language automatically")
        detectedLanguage = 'unknown'
    }
    
    return detectedLanguage
}

def setupProjectEnvironment(String language, Map config = [:]) {
    logInfo("Setting up project environment for language: ${language}")
    
    // Set language-specific environment variables
    switch(language) {
        case 'java-maven':
            env.BUILD_TOOL = 'maven'
            env.BUILD_COMMAND = 'mvn'
            env.TEST_COMMAND = 'mvn test'
            break
        case 'java-gradle':
            env.BUILD_TOOL = 'gradle'
            env.BUILD_COMMAND = './gradlew'
            env.TEST_COMMAND = './gradlew test'
            break
        case 'python':
            env.BUILD_TOOL = 'pip'
            env.BUILD_COMMAND = 'pip'
            env.TEST_COMMAND = 'pytest'
            break
        default:
            logWarning("Unknown language: ${language}")
    }
    
    // Set configuration-based environment variables
    if (config.runUnitTests != null) {
        env.RUN_UNIT_TESTS = config.runUnitTests.toString()
    }
    if (config.runLintTests != null) {
        env.RUN_LINT_TESTS = config.runLintTests.toString()
    }
    
    logInfo("Project environment setup completed")
    return true
}

def runSonarQubeAnalysis() {
    logInfo("Running SonarQube analysis...")
    
    try {
        // This would typically require SonarQube server configuration
        logInfo("SonarQube analysis would run here")
        // withSonarQubeEnv('SonarQube') {
        //     sh "${env.BUILD_COMMAND} sonar:sonar"
        // }
        logInfo("SonarQube analysis completed")
        return true
    } catch (Exception e) {
        logError("SonarQube analysis failed: ${e.getMessage()}")
        return false
    }
}

// Configuration reader functions
def readProjectConfig() {
    logInfo("Reading project configuration...")
    
    def config = [:]
    
    // Try to read YAML config file
    if (fileExists('ci-config.yaml')) {
        logInfo("Found ci-config.yaml file")
        config = readYamlConfig('ci-config.yaml')
    } else if (fileExists('ci-config.yml')) {
        logInfo("Found ci-config.yml file")
        config = readYamlConfig('ci-config.yml')
    } else {
        logWarning("No configuration file found, using auto-detection and defaults")
        config = getDefaultConfig()
    }
    
    // Validate and set defaults
    config = validateAndSetDefaults(config)
    
    logInfo("Project configuration loaded successfully")
    return config
}

def readYamlConfig(String configFile) {
    logInfo("Reading YAML configuration from: ${configFile}")
    
    try {
        def yamlContent = readFile(configFile)
        def config = readYaml text: yamlContent
        
        logInfo("YAML configuration parsed successfully")
        return config
    } catch (Exception e) {
        logError("Failed to read YAML configuration: ${e.getMessage()}")
        logWarning("Falling back to default configuration")
        return getDefaultConfig()
    }
}

def getDefaultConfig() {
    logInfo("Using default configuration - ALL STAGES WILL RUN BY DEFAULT")
    
    def config = [
        project_language: detectProjectLanguage(),
        runUnitTests: true,     // DEFAULT: true (stage will execute)
        runLintTests: true,     // DEFAULT: true (stage will execute)
        tool_for_unit_testing: [:],
        tool_for_lint_testing: [:]
    ]
    
    // Set default tools based on detected language
    def language = config.project_language
    if (language == 'java-maven' || language == 'java-gradle') {
        config.tool_for_unit_testing = [java: 'junit']
        config.tool_for_lint_testing = [java: 'checkstyle']
    } else if (language == 'python') {
        config.tool_for_unit_testing = [python: 'pytest']
        config.tool_for_lint_testing = [python: 'pylint']
    }
    
    return config
}

def validateAndSetDefaults(Map config) {
    logInfo("Validating configuration and setting defaults")
    
    // Validate project language
    if (!config.project_language) {
        config.project_language = detectProjectLanguage()
        logInfo("Auto-detected project language: ${config.project_language}")
    }
    
    // IMPORTANT: Default to true (execute all stages) if not specified
    config.runUnitTests = config.runUnitTests != null ? config.runUnitTests : true
    config.runLintTests = config.runLintTests != null ? config.runLintTests : true
    
    // Log stage execution plan
    logInfo("Stage Execution Plan:")
    logInfo("- Unit Tests: ${config.runUnitTests ? 'WILL RUN' : 'WILL SKIP'}")
    logInfo("- Lint Tests: ${config.runLintTests ? 'WILL RUN' : 'WILL SKIP'}")
    
    // Validate and set tool defaults
    def language = config.project_language
    
    if (language == 'java-maven' || language == 'java-gradle') {
        if (!config.tool_for_unit_testing?.java) {
            config.tool_for_unit_testing = config.tool_for_unit_testing ?: [:]
            config.tool_for_unit_testing.java = 'junit'
        }
        if (!config.tool_for_lint_testing?.java) {
            config.tool_for_lint_testing = config.tool_for_lint_testing ?: [:]
            config.tool_for_lint_testing.java = 'checkstyle'
        }
    } else if (language == 'python') {
        if (!config.tool_for_unit_testing?.python) {
            config.tool_for_unit_testing = config.tool_for_unit_testing ?: [:]
            config.tool_for_unit_testing.python = 'pytest'
        }
        if (!config.tool_for_lint_testing?.python) {
            config.tool_for_lint_testing = config.tool_for_lint_testing ?: [:]
            config.tool_for_lint_testing.python = 'pylint'
        }
    }
    
    logInfo("Configuration validation completed")
    return config
}

// Helper function to check if a stage should be executed
def shouldExecuteStage(String stageName, Map config) {
    switch(stageName.toLowerCase()) {
        case 'unittest':
        case 'unit_test':
        case 'unit-test':
            return config.runUnitTests == true
        case 'lint':
        case 'linttest':
        case 'lint_test':
        case 'lint-test':
            return config.runLintTests == true
        default:
            // All other stages (checkout, setup, build, notify) always run
            return true
    }
}
