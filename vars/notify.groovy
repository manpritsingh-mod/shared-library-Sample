// Main Slack notification function - calls specific Slack methods
def slackNotify(String message, String channel = '', String color = 'good') {
    core_utils.logInfo("Sending Slack notification...")
    
    try {
        // Call Slack notification logic
        return task_slackNotification(message, channel, color)
    } catch (Exception e) {
        core_utils.logError("Slack notification failed: ${e.getMessage()}")
        return false
    }
}

// Slack notification logic - main logic separated for easy changes
def task_slackNotification(String message, String channel, String color) {
    core_utils.logInfo("Slack notification logic execution...")
    
    try {
        // Default channel if not provided
        def targetChannel = channel ?: '#ci-notifications'
        
        // Format message with build info
        def formattedMessage = formatSlackMessage(message)
        
        core_utils.logInfo("Sending to Slack channel: ${targetChannel}")
        core_utils.logInfo("Message: ${formattedMessage}")
        
        // Send Slack notification using Jenkins Slack plugin
        slackSend(
            channel: targetChannel,
            color: color,
            message: formattedMessage,
            teamDomain: env.SLACK_TEAM_DOMAIN ?: 'your-team',
            token: env.SLACK_TOKEN ?: ''
        )
        
        core_utils.logInfo("Slack notification sent successfully")
        return true
    } catch (Exception e) {
        core_utils.logError("Slack notification logic failed: ${e.getMessage()}")
        return false
    }
}

// Slack build status notification - calls Slack notification with status formatting
def slackNotifyBuildStatus(String status, String message = '') {
    core_utils.logInfo("Sending Slack build status notification...")
    
    try {
        // Call build status logic
        return task_slackBuildStatus(status, message)
    } catch (Exception e) {
        core_utils.logError("Slack build status notification failed: ${e.getMessage()}")
        return false
    }
}

// Slack build status logic - main logic separated
def task_slackBuildStatus(String status, String message) {
    core_utils.logInfo("Slack build status logic execution...")
    
    def color = getStatusColor(status)
    def statusMessage = formatBuildStatusMessage(status, message)
    
    // Call main Slack notification method
    return slackNotify(statusMessage, '', color)
}

// Main email notification function - calls specific email methods
def emailNotify(String message, String recipients = '', String subject = '') {
    core_utils.logInfo("Sending email notification...")
    
    try {
        // Call email notification logic
        return task_emailNotification(message, recipients, subject)
    } catch (Exception e) {
        core_utils.logError("Email notification failed: ${e.getMessage()}")
        return false
    }
}

// Email notification logic - main logic separated for easy changes
def task_emailNotification(String message, String recipients, String subject) {
    core_utils.logInfo("Email notification logic execution...")
    
    try {
        // Default recipients if not provided
        def targetRecipients = recipients ?: env.DEFAULT_EMAIL_RECIPIENTS ?: 'dev-team@company.com'
        
        // Default subject if not provided
        def emailSubject = subject ?: "Jenkins Build Notification - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        
        // Format email body
        def emailBody = formatEmailMessage(message)
        
        core_utils.logInfo("Sending email to: ${targetRecipients}")
        core_utils.logInfo("Subject: ${emailSubject}")
        
        // Send email using Jenkins email plugin
        emailext(
            to: targetRecipients,
            subject: emailSubject,
            body: emailBody,
            mimeType: 'text/html'
        )
        
        core_utils.logInfo("Email notification sent successfully")
        return true
    } catch (Exception e) {
        core_utils.logError("Email notification logic failed: ${e.getMessage()}")
        return false
    }
}

// Email build status notification - calls email notification with status formatting
def emailNotifyBuildStatus(String status, String message = '') {
    core_utils.logInfo("Sending email build status notification...")
    
    try {
        // Call email build status logic
        return task_emailBuildStatus(status, message)
    } catch (Exception e) {
        core_utils.logError("Email build status notification failed: ${e.getMessage()}")
        return false
    }
}

// Email build status logic - main logic separated
def task_emailBuildStatus(String status, String message) {
    core_utils.logInfo("Email build status logic execution...")
    
    def statusSubject = formatBuildStatusSubject(status)
    def statusMessage = formatBuildStatusMessage(status, message)
    
    // Call main email notification method
    return emailNotify(statusMessage, '', statusSubject)
}

// Utility method to get color based on build status
def getStatusColor(String status) {
    switch(status.toLowerCase()) {
        case 'success':
        case 'passed':
            return 'good'
        case 'failure':
        case 'failed':
            return 'danger'
        case 'unstable':
        case 'warning':
            return 'warning'
        case 'aborted':
            return '#808080'
        default:
            return '#439FE0'
    }
}

// Utility method to format Slack message with build context
def formatSlackMessage(String message) {
    def buildInfo = getBuildInfo()
    
    def formattedMessage = """
*Project*: ${buildInfo.jobName}
*Build*: #${buildInfo.buildNumber}
*Branch*: ${buildInfo.branch}
*Status*: ${message}
*Duration*: ${buildInfo.duration}
*Build URL*: ${buildInfo.buildUrl}
"""
    return formattedMessage
}

// Utility method to format build status message
def formatBuildStatusMessage(String status, String additionalMessage) {
    def buildInfo = getBuildInfo()
    
    def statusIcon = getStatusIcon(status)
    def message = "${statusIcon} Build ${status.toUpperCase()}"
    
    if (additionalMessage) {
        message += "\n\n*Details*: ${additionalMessage}"
    }
    
    return message
}

// Utility method to format build status subject for email
def formatBuildStatusSubject(String status) {
    def statusIcon = getStatusIcon(status)
    return "${statusIcon} ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${status.toUpperCase()}"
}

// Utility method to format email message with HTML
def formatEmailMessage(String message) {
    def buildInfo = getBuildInfo()
    
    def emailBody = """
<html>
<body>
<h2>Jenkins Build Notification</h2>
<table border="1" cellpadding="5" cellspacing="0">
    <tr><td><strong>Project</strong></td><td>${buildInfo.jobName}</td></tr>
    <tr><td><strong>Build Number</strong></td><td>#${buildInfo.buildNumber}</td></tr>
    <tr><td><strong>Branch</strong></td><td>${buildInfo.branch}</td></tr>
    <tr><td><strong>Duration</strong></td><td>${buildInfo.duration}</td></tr>
    <tr><td><strong>Build URL</strong></td><td><a href="${buildInfo.buildUrl}">${buildInfo.buildUrl}</a></td></tr>
</table>
<br>
<p><strong>Message:</strong></p>
<p>${message}</p>
<br>
<p><em>This is an automated message from Jenkins CI/CD pipeline.</em></p>
</body>
</html>
"""
    return emailBody
}

// Utility method to get status icon
def getStatusIcon(String status) {
    switch(status.toLowerCase()) {
        case 'success':
        case 'passed':
            return '✅'
        case 'failure':
        case 'failed':
            return '❌'
        case 'unstable':
        case 'warning':
            return '⚠️'
        case 'aborted':
            return '⏹️'
        case 'running':
        case 'started':
            return '🔄'
        default:
            return 'ℹ️'
    }
}

// Utility method to get build information
def getBuildInfo() {
    def duration = currentBuild.duration ? "${currentBuild.duration}ms" : "N/A"
    if (currentBuild.duration && currentBuild.duration > 1000) {
        duration = "${Math.round(currentBuild.duration / 1000)}s"
    }
    if (currentBuild.duration && currentBuild.duration > 60000) {
        duration = "${Math.round(currentBuild.duration / 60000)}m"
    }
    
    return [
        jobName: env.JOB_NAME ?: 'Unknown Job',
        buildNumber: env.BUILD_NUMBER ?: 'Unknown',
        branch: env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'Unknown',
        duration: duration,
        buildUrl: env.BUILD_URL ?: 'N/A',
        buildUser: env.BUILD_USER ?: 'Jenkins'
    ]
}

// Comprehensive notification method - sends to both Slack and email
def notifyAll(String status, String message = '', Map options = [:]) {
    core_utils.logInfo("Sending comprehensive notifications...")
    
    def results = [
        slackSent: false,
        emailSent: false,
        overallSuccess: false
    ]
    
    try {
        // Send Slack notification if enabled
        if (options.enableSlack != false) {
            results.slackSent = slackNotifyBuildStatus(status, message)
        }
        
        // Send email notification if enabled
        if (options.enableEmail != false) {
            results.emailSent = emailNotifyBuildStatus(status, message)
        }
        
        results.overallSuccess = (options.enableSlack != false ? results.slackSent : true) && 
                                (options.enableEmail != false ? results.emailSent : true)
        
        if (results.overallSuccess) {
            core_utils.logInfo("All notifications sent successfully")
        } else {
            core_utils.logWarning("Some notifications failed")
        }
        
        return results
    } catch (Exception e) {
        core_utils.logError("Comprehensive notification failed: ${e.getMessage()}")
        results.overallSuccess = false
        return results
    }
}
