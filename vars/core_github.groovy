def checkout(String repoUrl = '', String branch = 'main') {
    core_utils.logInfo("Starting GitHub checkout process...")
    
    if (repoUrl) {
        // Checkout specific repository
        return checkoutRepo(repoUrl, branch)
    } else {
        // Checkout current repository (SCM checkout)
        return checkoutCurrentRepo()
    }
}

def checkoutRepo(String repoUrl, String branch) {
    core_utils.logInfo("Checking out repository: ${repoUrl}, branch: ${branch}")
    
    try {
        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/${branch}"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                [$class: 'CleanBeforeCheckout'],
                [$class: 'CloneOption', depth: 1, noTags: false, reference: '', shallow: true]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [[url: repoUrl]]
        ])
        
        core_utils.logInfo("Repository checkout completed successfully")
        return true
    } catch (Exception e) {
        core_utils.logError("Repository checkout failed: ${e.getMessage()}")
        return false
    }
}

def checkoutCurrentRepo() {
    core_utils.logInfo("Checking out current repository from SCM")
    
    try {
        checkout scm
        core_utils.logInfo("SCM checkout completed successfully")
        return true
    } catch (Exception e) {
        core_utils.logError("SCM checkout failed: ${e.getMessage()}")
        return false
    }
}

def getBranchInfo() {
    core_utils.logInfo("Getting branch information...")
    
    def branchInfo = [:]
    
    try {
        // Get current branch name
        def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
        if (branchName.startsWith('origin/')) {
            branchName = branchName.replace('origin/', '')
        }
        
        branchInfo.name = branchName
        branchInfo.commit = env.GIT_COMMIT ?: 'unknown'
        
        // Get additional git info if available
        if (isUnix()) {
            try {
                branchInfo.shortCommit = sh(
                    script: 'git rev-parse --short HEAD',
                    returnStdout: true
                ).trim()
                branchInfo.author = sh(
                    script: 'git log -1 --pretty=format:"%an"',
                    returnStdout: true
                ).trim()
                branchInfo.message = sh(
                    script: 'git log -1 --pretty=format:"%s"',
                    returnStdout: true
                ).trim()
            } catch (Exception e) {
                core_utils.logWarning("Could not get detailed git info: ${e.getMessage()}")
            }
        } else {
            try {
                branchInfo.shortCommit = bat(
                    script: '@git rev-parse --short HEAD',
                    returnStdout: true
                ).trim()
                branchInfo.author = bat(
                    script: '@git log -1 --pretty=format:"%an"',
                    returnStdout: true
                ).trim()
                branchInfo.message = bat(
                    script: '@git log -1 --pretty=format:"%s"',
                    returnStdout: true
                ).trim()
            } catch (Exception e) {
                core_utils.logWarning("Could not get detailed git info: ${e.getMessage()}")
            }
        }
        
        core_utils.logInfo("Branch Info:")
        core_utils.logInfo("- Name: ${branchInfo.name}")
        core_utils.logInfo("- Commit: ${branchInfo.commit}")
        core_utils.logInfo("- Short Commit: ${branchInfo.shortCommit ?: 'N/A'}")
        core_utils.logInfo("- Author: ${branchInfo.author ?: 'N/A'}")
        core_utils.logInfo("- Message: ${branchInfo.message ?: 'N/A'}")
        
        return branchInfo
    } catch (Exception e) {
        core_utils.logError("Failed to get branch info: ${e.getMessage()}")
        return [
            name: 'unknown',
            commit: 'unknown',
            shortCommit: 'unknown',
            author: 'unknown',
            message: 'unknown'
        ]
    }
}

def validateRepoAccess(String repoUrl = '') {
    core_utils.logInfo("Validating repository access...")
    
    try {
        if (repoUrl) {
            // Validate specific repository access
            return validateSpecificRepo(repoUrl)
        } else {
            // Validate current repository access
            return validateCurrentRepo()
        }
    } catch (Exception e) {
        core_utils.logError("Repository validation failed: ${e.getMessage()}")
        return false
    }
}

def validateSpecificRepo(String repoUrl) {
    core_utils.logInfo("Validating access to repository: ${repoUrl}")
    
    try {
        def command = "git ls-remote ${repoUrl} HEAD"
        def result
        
        if (isUnix()) {
            result = sh(
                script: command,
                returnStatus: true
            )
        } else {
            result = bat(
                script: command,
                returnStatus: true
            )
        }
        
        if (result == 0) {
            core_utils.logInfo("Repository access validated successfully")
            return true
        } else {
            core_utils.logError("Repository access validation failed")
            return false
        }
    } catch (Exception e) {
        core_utils.logError("Repository validation error: ${e.getMessage()}")
        return false
    }
}

def validateCurrentRepo() {
    core_utils.logInfo("Validating current repository access")
    
    try {
        def command = 'git rev-parse --is-inside-work-tree'
        def result
        
        if (isUnix()) {
            result = sh(
                script: command,
                returnStatus: true
            )
        } else {
            result = bat(
                script: command,
                returnStatus: true
            )
        }
        
        if (result == 0) {
            core_utils.logInfo("Current repository validation successful")
            return true
        } else {
            core_utils.logWarning("Not in a valid git repository")
            return false
        }
    } catch (Exception e) {
        core_utils.logError("Current repository validation error: ${e.getMessage()}")
        return false
    }
}
