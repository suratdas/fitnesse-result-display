package com.crud.rest.configuration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.service.FitnesseSuiteService;
import com.crud.rest.service.SuiteExecutionServiceImpl;

//@Component
public class ScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	private SuiteExecutionServiceImpl suiteExecutionService;

	@Autowired
	private FitnesseSuiteService fitnesseSuiteService;

	private boolean isAnySuiteAlreadyRunning;

	// @Scheduled(fixedRate=60000)
	public void triggerTestExecution(String fitnesseUsername, String fitnessePassword) {
		log.info("Test execution started at " + dateFormat.format(new Date()));

		List<FitnesseSuite> fitnesseSuites = fitnesseSuiteService.findAllSuites();
		fitnesseSuites.removeIf(eachSuite -> !eachSuite.getShouldRun());

		// If any of the suite is already running, don't run again. Abort the whole run for all suites. We can change the logic in future.
		isAnySuiteAlreadyRunning = false;
		fitnesseSuites.forEach((eachSuite) -> {
			if (eachSuite.isRunning()) {
				System.out.println(String.format("Error: %s suite is still running. Cannot run again. Aborting.",
						eachSuite.getSuiteName()));
				isAnySuiteAlreadyRunning = true;
			}
		});

		if (isAnySuiteAlreadyRunning)
			return;

		ExecutorService executor = Executors.newFixedThreadPool(fitnesseSuites.size());

		for (FitnesseSuite fitnesseSuite : fitnesseSuites)
			executor.submit(new TestExecution(fitnesseSuite, fitnesseUsername, fitnessePassword));

		executor.shutdown();

		System.out.println(String.format("Running %d suites in parallel...", fitnesseSuites.size()));

		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("All suites run successfully");

	}

	public class TestExecution implements Runnable {

		private FitnesseSuite fitnesseSuite;
		private String fitnesseUsername;
		private String fitnessePassword;

		TestExecution(FitnesseSuite fitnesseSuite, String fitnesseUsername, String fitnessePassword) {
			this.fitnesseSuite = fitnesseSuite;
			this.fitnesseUsername = fitnesseUsername;
			this.fitnessePassword = fitnessePassword;
		}

		@Override
		public void run() {
			fitnesseSuiteService.markTestRunningStatus(fitnesseSuite, true);
			suiteExecutionService.setLastExecutionTime(new Date().toString());
			int suiteId = fitnesseSuite.getSuiteId();

			suiteExecutionService.executeSuite(suiteId, fitnesseSuite.getSuiteUrl(), fitnesseUsername,
					fitnessePassword);
			int passedTestCount = suiteExecutionService.getPassedTestCaseCount(suiteId);
			int failedTestCount = suiteExecutionService.getFailedTestCaseCount(suiteId);
			fitnesseSuite.setPassedTests(passedTestCount);
			fitnesseSuite.setFailedTests(failedTestCount);
			fitnesseSuite.setTotalTests(passedTestCount + failedTestCount);
			fitnesseSuiteService.markTestRunningStatus(fitnesseSuite, false);
		}

	}

}
