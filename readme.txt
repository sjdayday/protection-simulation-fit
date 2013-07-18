7/13/13  

protection-scenario-fit enables fitlibrary acceptance tests and production simulations to be run against the Market for Protection model (protection-simulation)

Pre-requisites:
 
This project depends on three projects: 
  protection-simulation:  Simulates the Market for Protection 
    https://github.com/sjdayday/protection-simulation.git
  simulation-scenario:  Framework for running and replicating simulations
    https://github.com/sjdayday/simulation-scenario.git
  simulation-scenario-fit:  Packaging of fitnesse and fitlibrary acceptance testing frameworks, and simulation-related test fixtures.
    https://github.com/sjdayday/simulation-scenario-fit.git     
  
Although these projects create Maven artifacts, those are not yet publicly available, so for now, 
please download the projects and install them in your local repository:  mvn package; mvn install.

The readme for simulation-scenario-fit explains additional manual steps necessary to get fitnesse / fitlibrary running.
Once you have gotten fitnesse running and have run the acceptance tests for the simulation-scenario-fit project, you're ready to set up this project.

----------------

Set up: 

Set up environment variable SCENARIO_ROOT pointing to a directory where scenario output will be generated, e.g., export SCENARIO_ROOT=/Users/stevedoubleday/scenario
Alternatively, you may want to define SCENARIO_ROOT on the startup of fitnesse, e.g., java -DSCENARIO_ROOT=/Users/stevedoubleday/scenario [remaining fitnesse parameters - see below]  

I suggest you start a separate instance of fitnesse for this project.  
A sample fitnesse startup command for this project (change the directories to match your configuration):

java -DPROTTARGET=/Users/stevedoubleday/git/protection-simulation-fit/target -jar fitnesse.jar -p 8091 -e 0 -d /Users/stevedoubleday/git/protection-simulation-fit/src/test/resources   

The PROTTARGET environment variable is required by the acceptance tests, and should point to the directory where this project's classes reside (by Maven convention, the "target" directory).
Similarly, point the "-d" parameter of fitnesse to the /src/test/resources/ directory in the project; that's where the tests reside. 
-----------------
   
Running the Acceptance tests:

1) Start fitnesse as set up above.  You'll need to set two environment variables, one in your environment profile, and the other as a fitnesse command-line argument: 
2) In a browser, navigate to fitnesse:  http://localhost:8091   [the port is specified on the fitnesse startup, e.g.: -p 8091 ]
3) You should see the fitnesse FrontPage; click on the link towards the bottom:  ProtectionSimulationFitAcceptanceTests
4) The next page is primarily the class path setup, and requires that the M2REPO and PROTTARGET variables be set to the appropriate locations on your system.
  You'll see "undefined variable" if the environment variables are not set.  
  If the paths are not correct or incomplete, you'll get class not found exceptions when you actually run the tests.    
5) Click on "Market For Protection"
6) On the Market for Protection page, click on Test in the left Nav.  25 tests should pass. 

To actually run the scenarios, click "Edit", and uncomment the last line:  from "|note|run scenarios|" to "|run scenarios|"
Click "Save" below the edit window, and then click "Test".

170 scenarios will be run.  This takes a number of minutes; fitnesse will be clocked-out the whole time. 
You will see files get created in your SCENARIO_ROOT directory:
 ScenarioSet_x/  [directory for this scenario set; x an ascending integer from 1]
    parameterSpaceScenario_x.xml : persisted parameter space for this scenario set
    scenario_set_summary_x_test scenario set.csv :  comma-delimited file of summary simulation results
    logs/  [directory with one log file for each scenario point:  170 files in this case]  

The nice shutdown for fitnesse is: http://localhost:8091/?responder=shutdown

What's going on under the covers?  Until I document this better, maybe start at the top, and follow the bouncing ball: edu.uci.imbs.actor.fit.ProtectionFixture

---------------

Questions:  stevedoubleday [at] gmail [dot] com

Thanks for your interest!

Steve Doubleday
  