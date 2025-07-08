shared-library-UnifiedCI
│
├── src/                          		~ Pure Groovy classes (defination in src and execution in vars)-- this keep the logic clean, reusable and testable
|	├── python-script.groovy      		~ these will be containing the files like (requirement.txt, and many more other)  // return python related commands (eg - 										 pip insall/env setups)
│   ├── maven-script.groovy       		~ it will be containing the pom file  // return maven lint/build/test command strings
│   ├── gradle-script.groovy      		~ it will be containing the build file  // return gradle command strings (build/test)
│   ├── Logger.groovy     		 	    ~ for the log message !!! --------------needed or not needed----------------------
│   ├── GitHubManager.groovy     		~ for the repoURL, tokenID !!!  (basically it Handle API calls, parse data)
│
├── vars/
│   ├── core_github.groovy      									~ Core GitHub related functionalities
|	|			├──  checkout
|	|			|			├──  checkoutRepo(repoUrl, branch)		~ Main Checkout Function (repoURL or SCM)
|	|			├──	 validateRepoAccess()							~ validate repository accessibility			
|	|
│   ├── core_utils.groovy       						~ Utility/helper functions
|	|			├──	 logInfo()							~ Waring/Error logs (Printing timeStamp, message)
|	|			├──	 logWarning()						~ TimeStamp logging 
|	|			├──	 logError()
|	|			├──	 setupEnvironment() 				~ Set build Environment Variables 
|	|			├──	 detectProjectLanguage() 
|	|			|				├──	 task_languageDetection()	~ Auto detect the Java Maven/Gradle pr python project	
|	|			├──	 task_languageDetection() 			~ main logic has to be written here (Auto detect the Java Maven/Gradle pr python project)
|	|			├──	 runSonarQubeAnalysis() 			~ Analyse the bugs, Vulnerabilities, Duplicate code, Techincal issues (used for the security reasons)
|	|			├──	 readProjectConfig()
|	|			|				├──	 readYamlConfig('ci-config.yaml')
|	|			├──	 readYamlConfig('ci-config.yaml') 	~ Reading the YAML file through this 
|	|			├──	 getDefaultConfig()					~ Using default configuration - All stages will run by default
|	|
│   ├── lint_utils.groovy        						
|	|			├──  runLint()		
|	|			|			├──  task_lintStage()
|	|			|							├──  runJavaLint()							~ Language-specific lint methods
|	|			|							|			├──  task_JavaLintLogic()		~ main logic has to be written here
|	|			|							├──  runPythonLint()						~ Language-specific lint methods
|	|			|										├──  task_pythonLintLogic()		~ main logic has to be written here
|	|			|
|	|			├──	 getLintTool()					~ Configuration-based tool selection	
|	|
|	|
│   ├── core_test.groovy        						~ Core testing functionality (run tests)
|	|			├──	 runTest()							~ Main runTest methods, returns test results object
|	|			|		├──	 runUnitTest()
|	|			|		|		├──  task_unitTestStage()
|	|			|		|					├──  runJavaUnitTest()							~ Language-specific unit test methods
|	|			|		|					|			├──  task_javaUnitTestLogic()		~ main logic has to be written here
|	|			|		|					├──  runPythonUnitTest()						~ Language-specific unit test methods
|	|			|		|								├──  task_pythonUnitTestLogic()		~ main logic has to be written here
|	|			|		├──	 runUnitTestCoverage()
|	|			|						├──	 runJavaCoverage()	
|	|			|						├──	 runPythonCoverage()
|	|			├──	 getUnitTestTool()				~ Configuration-based tool selection
|	|			
|	|
│   ├── notify.groovy          			    						~ Notification functions (Slack, email notifications)
|	|			├──  slackNotify(message)
|	|			|				├──	 task_slackNotification()		~ main logic has to be written here(Core Slack message)
|	|			├──	 task_slackNotification()
|	|			├──	 slackNotifyBuildStatus( status, message) 		~ sending it with the build status and the message
|	|			|				├──	 task_slackBuildStatus(status, message)			
|	|			├──	 task_slackBuildStatus(status, message)			~ main logic has to be written here (Status specific slack message)
|	|			├──	 emailNotify()
|	|							├──	 task_emailNotification()			~ main logic has to be written here
|	|			├──	 emailNotifyBuildStatus( status, message) 		~ sending it with the build status and the message
|	|							├──	 task_emailBuildStatus(status, message)		~ main logic has to be written here (Status specific email message) 
|	|		
│   ├── sendReports.groovy      									~ Generate and send reports (test/coverage/build reports)
|	|			├──  generateReport()
|	|			|				├──	 task_reportGeneration()		~ Main Report
|	|			├──	 task_reportGeneration()						~ main logic has to be written here (Language-specific methods)
|	|			|				├──	 generateTestReport()			

|	|			|				├──	 generateBuildReport()
|	|			├──	 generateTestReport()							~ Publishes JUnit reports, pytest reports
|	|			|				├──	 task_testReportGeneration()
|	|			|									├──	 generateJavaMavenTestReport()
|	|			|									├──	 generateJavaGradleTestReport()
|	|			|									├──	 generatePythonTestReport()
|	|			├──	 generateBuildReport()												~ Generates build summaries	
|	|			|				├──	 task_buildReportGeneration()
|	|			|
|	|			├──	 sendReport()						~ Report Sending through 
|	├──  core_build.groovy 							                                                                                                																			
|				├──	 buildLanguages()									~ Main Entry point , for the language specific builds 
|				|			├──	 buildJava('maven')   
|				|			├──	 buildJava('gradle')    
|				|			├──	 buildPython()    
|				|    		 
|				├──	 buildJavaApp() 
|				|			├──	 installDependencies()  
|				|			├──	 buildMaven()   
|				|			├──	 buildGradle() 
|				|			  
|				├──	 buildMaven()
|				|			├──	 task_mavenBuild()  					~ Actual Build logic 
|				├──	 buildGradle() 
|				|			├──	 task_gradleBuild()    					~ Actual Build logic 
|				├──	 buildPythonApp() 
|				|			├──	 installDependencies()  
|				|			├──	 task_pythonBuild()						~ Actual Build logic 
|				├──	 installDependencies()	
|				|			├──	 installJavaDependencies()
|				|			├──	 installPythonDependencies()
|				|
|				├──	 installJavaDependencies()
|				|			├──	 task_mavenDependencies()				~ Actual Dependencies logic
|				|			├──	 task_gradleDependencies()				~ Actual Dependencies logic
|				├──	 installPythonDependencies()
|							├──	 task_pythonDependencies()				~ Actual Dependencies logic
|	
│
│
├── templates/
│   ├── java(maven, gradle)-template.groovy   	~ Basic Java pipeline stages (checkout, setup, install dep, Lint test, build, unit test, notify )
│   ├── python-template.groovy  				~ Basic Python pipeline stages (checkout, setup, install dep, Lint test, build, unit test, notify )
│
|
└── README.md
