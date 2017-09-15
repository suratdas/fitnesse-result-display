package com.crud.rest.configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.model.TestExecutionSettings;
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

	private boolean isAnySuiteAlreadyRunning, scheduledExecution = true;

	public ScheduledTasks(SuiteExecutionServiceImpl suiteExecutionService, FitnesseSuiteService fitnesseSuiteService,
			boolean scheduledExecution) {
		this.suiteExecutionService = suiteExecutionService;
		this.fitnesseSuiteService = fitnesseSuiteService;
		this.scheduledExecution = scheduledExecution;
	}

	public ScheduledTasks() {
	}

	// @Scheduled(fixedRate=60000)
	public void triggerTestExecution(String fitnesseUsername, String fitnessePassword, boolean forcedExecution) {
		log.info("Test execution started at " + dateFormat.format(new Date()));

		List<FitnesseSuite> fitnesseSuites = fitnesseSuiteService.findAllSuites();
		fitnesseSuites.removeIf(eachSuite -> !eachSuite.getShouldRun());

		//For forced execution we don't check the status of previous execution.
		if (!forcedExecution) {
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
		}

		TestExecutionSettings testExecutionsettings = suiteExecutionService.findCurrentSettings();
		if (scheduledExecution) {
			Calendar date = Calendar.getInstance();
			long currentTime = date.getTimeInMillis();
			//TODO Ensure that you have right value in database. Set 1440 if you want to execute every day.
			Date afterAddingIntervalInMinutes = new Date(
					currentTime + (testExecutionsettings.getExecutionInterval() * 60000));
			testExecutionsettings.setNextExecutionTime(afterAddingIntervalInMinutes);
		}
		String decryptedFitnessePassword = null;
		if (fitnessePassword != null && fitnessePassword.length() > 1) {
			StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
			decryptor.setPassword(AppConfig.encryptionSeed);
			decryptedFitnessePassword = decryptor.decrypt(fitnessePassword);
		}

		testExecutionsettings.setRunning(true);

		suiteExecutionService.updateTestExecutionSettings(testExecutionsettings);

		ExecutorService executor = Executors.newFixedThreadPool(testExecutionsettings.getNumberOfExecutionThread());

		for (FitnesseSuite fitnesseSuite : fitnesseSuites)
			executor.submit(new TestExecution(fitnesseSuite, fitnesseUsername, decryptedFitnessePassword));

		executor.shutdown();

		System.out.println(String.format(new Date() + " : Running %d suites in parallel...", fitnesseSuites.size()));

		try {
			executor.awaitTermination(testExecutionsettings.getConnectionTimeOutInMinutes(), TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		testExecutionsettings.setRunning(false);
		suiteExecutionService.updateTestExecutionSettings(testExecutionsettings);

		System.out.println(new Date() + ": Execution completed. Check log file for the details.");

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
			fitnesseSuite.setRunning(true);
			fitnesseSuite.setLastExecutionTime(new Date());
			fitnesseSuiteService.updateTestSuite(fitnesseSuite);

			try {
				suiteExecutionService.executeSuite(fitnesseSuite, fitnesseUsername, fitnessePassword);
			} catch (Exception e) {
				System.out.println(fitnesseSuite.getSuiteName() + " threw an error.\n" + e);
			}

			int passedTestCount = suiteExecutionService.getPassedTestCaseCount(fitnesseSuite.getSuiteId());
			int failedTestCount = suiteExecutionService.getFailedTestCaseCount(fitnesseSuite.getSuiteId());
			fitnesseSuite.setPassedTests(passedTestCount);
			fitnesseSuite.setFailedTests(failedTestCount);
			fitnesseSuite.setTotalTests(passedTestCount + failedTestCount);
			fitnesseSuite.setRunning(false);
			fitnesseSuiteService.updateTestSuite(fitnesseSuite);
		}

	}

}
