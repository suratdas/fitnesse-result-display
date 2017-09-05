package com.crud.rest.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.service.FitnesseSuiteService;
import com.crud.rest.service.SuiteExecutionServiceImpl;
import com.crud.rest.service.SuiteExecutionServiceImpl.TestResultType;

@Component
public class ScheduledTasks {

	// private static final Logger log =
	// LoggerFactory.getLogger(ScheduledTasks.class);

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	private SuiteExecutionServiceImpl suiteExecutionService;

	@Autowired
	private FitnesseSuiteService fitnesseSuiteService;

	// TODO Check if this can be configured externally or from database
	@Scheduled(fixedRate = 60000)
	public void reportCurrentTime() {
		// log.info("The time is now {}", dateFormat.format(new Date()));
		System.out.println("The time is now " + dateFormat.format(new Date()));

		// Ensure nothing is running currently.

		// Update the last_execution_time for all enabled suite, also update the
		// next_execution_time and is_running
		
		//Get fitnesse username and password externally

		List<FitnesseSuite> fitnesseSuites = fitnesseSuiteService.findAllSuites();
		fitnesseSuites.removeIf(eachSuite -> eachSuite.getShouldRun() == false);

		for (FitnesseSuite fitnesseSuite : fitnesseSuites) {
			fitnesseSuiteService.markTestRunning(fitnesseSuite, true);
			suiteExecutionService.executeSuite(fitnesseSuite.getSuiteId(), fitnesseSuite.getSuiteUrl(), null, null);
			int passedTests = suiteExecutionService.getTestCaseCount(fitnesseSuite.getSuiteId(), TestResultType.Passed);
			int failedTests = suiteExecutionService.getTestCaseCount(fitnesseSuite.getSuiteId(), TestResultType.Failed);
			fitnesseSuite.setPassedTests(passedTests);
			fitnesseSuite.setFailedTests(failedTests);
			fitnesseSuite.setTotalTests(passedTests + failedTests);
			fitnesseSuiteService.markTestRunning(fitnesseSuite, false);
		}
		System.out.println("Processing done...");

		// Wait for all threads to finish

		// Mark as done, may be send an email

	}
}
