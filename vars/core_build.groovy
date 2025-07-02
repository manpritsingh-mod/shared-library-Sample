// Main build function - calls other methods based on language
def buildLanguages(String language, Map config = [:]) {
    core_utils.logInfo("Starting build process for language: ${language}")
    
    switch(language) {
        case 'java-maven':
            return buildJava('maven', config)
        case 'java-gradle':
            return buildJava('gradle', config)
        case 'python':
            return buildPython(config)
        default:
            core_utils.logError("Unsupported language: ${language}")
            return false
    }
}

// Java build function - calls specific Java build methods
def buildJava(String buildTool, Map config = [:]) {
    core_utils.logInfo("Building Java project with ${buildTool}")
    
    try {
        // Step 1: Install dependencies
        if (!installDependencies('java', buildTool, config)) {
            return false
        }
        
        // Step 2: Execute build based on tool
        if (buildTool == 'maven') {
            return buildMaven(config)
        } else if (buildTool == 'gradle') {
            return buildGradle(config)
        }
        
        return false
    } catch (Exception e) {
        core_utils.logError("Java build error: ${e.getMessage()}")
        return false
    }
}

// Maven build function - calls Maven-specific methods
def buildMaven(Map config = [:]) {
    core_utils.logInfo("Executing Maven build...")
    
    try {
        // Call Maven build logic method
        return task_mavenBuild(config)
    } catch (Exception e) {
        core_utils.logError("Maven build failed: ${e.getMessage()}")
        return false
    }
}

// Maven build logic - main logic separated for easy changes
def task_mavenBuild(Map config = [:]) {
    core_utils.logInfo("Maven build logic execution...")
    
    def command = 'mvn clean install'
    
    // Add common Maven options
    def mavenOpts = []
    mavenOpts.add('-DskipTests=true')  // Skip tests during build
    mavenOpts.add('-B')  // Batch mode
    mavenOpts.add('-V')  // Show version
    
    // Add custom options if provided
    if (config.mavenOptions) {
        mavenOpts.addAll(config.mavenOptions)
    }
    
    def fullCommand = "${command} ${mavenOpts.join(' ')}"
    core_utils.logInfo("Running: ${fullCommand}")
    
    if (isUnix()) {
        sh script: fullCommand
    } else {
        bat script: fullCommand
    }
    
    core_utils.logInfo("Maven build completed successfully")
    return true
}

// Gradle build function - calls Gradle-specific methods
def buildGradle(Map config = [:]) {
    core_utils.logInfo("Executing Gradle build...")
    
    try {
        // Call Gradle build logic method
        return task_gradleBuild(config)
    } catch (Exception e) {
        core_utils.logError("Gradle build failed: ${e.getMessage()}")
        return false
    }
}

// Gradle build logic - main logic separated for easy changes
def task_gradleBuild(Map config = [:]) {
    core_utils.logInfo("Gradle build logic execution...")
    
    // Use gradlew wrapper if available
    def gradleCommand = fileExists('gradlew') ? './gradlew' : 'gradle'
    def command = "${gradleCommand} build"
    
    // Add common Gradle options
    def gradleOpts = []
    gradleOpts.add('-x test')  // Skip tests during build
    gradleOpts.add('--no-daemon')  // Don't use daemon in CI
    gradleOpts.add('--stacktrace')  // Show stacktrace on failure
    
    // Add custom options if provided
    if (config.gradleOptions) {
        gradleOpts.addAll(config.gradleOptions)
    }
    
    def fullCommand = "${command} ${gradleOpts.join(' ')}"
    core_utils.logInfo("Running: ${fullCommand}")
    
    if (isUnix()) {
        sh script: fullCommand
    } else {
        bat script: fullCommand
    }
    
    core_utils.logInfo("Gradle build completed successfully")
    return true
}

// Python build function - calls Python-specific methods
def buildPython(Map config = [:]) {
    core_utils.logInfo("Building Python project")
    
    try {
        // Step 1: Install dependencies
        if (!installDependencies('python', 'pip', config)) {
            return false
        }
        
        // Step 2: Execute Python build
        return task_pythonBuild(config)
    } catch (Exception e) {
        core_utils.logError("Python build error: ${e.getMessage()}")
        return false
    }
}

// Python build logic - main logic separated for easy changes
def task_pythonBuild(Map config = [:]) {
    core_utils.logInfo("Python build logic execution...")
    
    // Check if setup.py exists for building
    if (fileExists('setup.py')) {
        def command = 'python setup.py build'
        core_utils.logInfo("Running: ${command}")
        
        if (isUnix()) {
            sh script: command
        } else {
            bat script: command
        }
    } else if (fileExists('pyproject.toml')) {
        // Modern Python projects with pyproject.toml
        def command = 'python -m build'
        core_utils.logInfo("Running: ${command}")
        
        if (isUnix()) {
            sh script: command
        } else {
            bat script: command
        }
    } else {
        core_utils.logInfo("No build file found (setup.py/pyproject.toml), skipping build step")
    }
    
    core_utils.logInfo("Python build completed successfully")
    return true
}

// Main dependencies function - calls specific dependency methods
def installDependencies(String language = '', String buildTool = '', Map config = [:]) {
    core_utils.logInfo("Installing dependencies for ${language} with ${buildTool}")
    
    try {
        switch(language) {
            case 'java':
                return installJavaDependencies(buildTool, config)
            case 'python':
                return installPythonDependencies(config)
            default:
                core_utils.logError("Unsupported language for dependency installation: ${language}")
                return false
        }
    } catch (Exception e) {
        core_utils.logError("Dependency installation failed: ${e.getMessage()}")
        return false
    }
}

// Java dependencies function - calls Java dependency logic
def installJavaDependencies(String buildTool, Map config = [:]) {
    core_utils.logInfo("Installing Java dependencies with ${buildTool}")
    
    try {
        if (buildTool == 'maven') {
            return task_mavenDependencies(config)
        } else if (buildTool == 'gradle') {
            return task_gradleDependencies(config)
        }
        return false
    } catch (Exception e) {
        core_utils.logError("Java dependency installation failed: ${e.getMessage()}")
        return false
    }
}

// Maven dependencies logic - main logic separated
def task_mavenDependencies(Map config = [:]) {
    core_utils.logInfo("Maven dependencies logic execution...")
    
    def command = 'mvn dependency:resolve -B'
    core_utils.logInfo("Running: ${command}")
    
    if (isUnix()) {
        sh script: command
    } else {
        bat script: command
    }
    
    core_utils.logInfo("Maven dependencies installed successfully")
    return true
}

// Gradle dependencies logic - main logic separated
def task_gradleDependencies(Map config = [:]) {
    core_utils.logInfo("Gradle dependencies logic execution...")
    
    def gradleCommand = fileExists('gradlew') ? './gradlew' : 'gradle'
    def command = "${gradleCommand} dependencies --no-daemon"
    core_utils.logInfo("Running: ${command}")
    
    if (isUnix()) {
        sh script: command
    } else {
        bat script: command
    }
    
    core_utils.logInfo("Gradle dependencies installed successfully")
    return true
}

// Python dependencies function - calls Python dependency logic
def installPythonDependencies(Map config = [:]) {
    core_utils.logInfo("Installing Python dependencies")
    
    try {
        return task_pythonDependencies(config)
    } catch (Exception e) {
        core_utils.logError("Python dependency installation failed: ${e.getMessage()}")
        return false
    }
}

// Python dependencies logic - main logic separated
def task_pythonDependencies(Map config = [:]) {
    core_utils.logInfo("Python dependencies logic execution...")
    
    // Install dependencies from requirements.txt
    if (fileExists('requirements.txt')) {
        def command = 'pip install -r requirements.txt'
        core_utils.logInfo("Running: ${command}")
        
        if (isUnix()) {
            sh script: command
        } else {
            bat script: command
        }
    }
    
    // Install dev dependencies if available
    if (fileExists('requirements-dev.txt')) {
        def command = 'pip install -r requirements-dev.txt'
        core_utils.logInfo("Running: ${command}")
        
        if (isUnix()) {
            sh script: command
        } else {
            bat script: command
        }
    }
    
    // Install project in development mode if setup.py exists
    if (fileExists('setup.py')) {
        def command = 'pip install -e .'
        core_utils.logInfo("Running: ${command}")
        
        if (isUnix()) {
            sh script: command
        } else {
            bat script: command
        }
    }
    
    core_utils.logInfo("Python dependencies installed successfully")
    return true
}
