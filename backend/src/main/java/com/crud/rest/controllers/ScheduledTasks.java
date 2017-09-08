package com.crud.rest.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.service.FitnesseSuiteService;
import com.crud.rest.service.SuiteExecutionServiceImpl;

@Component
public class ScheduledTasks {

	// private static final Logger log =
	// LoggerFactory.getLogger(ScheduledTasks.class);

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	private SuiteExecutionServiceImpl suiteExecutionService;

	@Autowired
	private FitnesseSuiteService fitnesseSuiteService;

	private boolean isAnySuiteAlreadyRunning;

	// TODO Check if this can be configured externally or from database
	@Scheduled(fixedRate=60000)
	public void reportCurrentTime() {
		// log.info("The time is now {}", dateFormat.format(new Date()));
		System.out.println("Test execution started at " + dateFormat.format(new Date()));

		// Update the last_execution_time for all enabled suite, also update the
		// next_execution_time

		// Get fitnesse username and password externally

		List<FitnesseSuite> fitnesseSuites = fitnesseSuiteService.findAllSuites();
		fitnesseSuites.removeIf(eachSuite -> !eachSuite.getShouldRun());

		// If any of the suite is already running, don't run again. Abort the
		// whole run for all suites. We can change the logic in future.
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
			executor.submit(new TestExecution(fitnesseSuite));

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

		FitnesseSuite fitnesseSuite;

		TestExecution(FitnesseSuite fitnesseSuite) {
			this.fitnesseSuite = fitnesseSuite;
		}

		@Override
		public void run() {
			fitnesseSuiteService.markTestRunningStatus(fitnesseSuite, true);
			int suiteId = fitnesseSuite.getSuiteId();
			suiteExecutionService.executeSuite(suiteId, fitnesseSuite.getSuiteUrl(), null, null);
			int passedTestCount = suiteExecutionService.getPassedTestCaseCount(suiteId);
			int failedTestCount = suiteExecutionService.getFailedTestCaseCount(suiteId);
			fitnesseSuite.setPassedTests(passedTestCount);
			fitnesseSuite.setFailedTests(failedTestCount);
			fitnesseSuite.setTotalTests(passedTestCount + failedTestCount);
			fitnesseSuiteService.markTestRunningStatus(fitnesseSuite, false);
		}

	}

}
