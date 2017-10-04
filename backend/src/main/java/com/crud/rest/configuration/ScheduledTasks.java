package com.crud.rest.configuration;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.model.TestExecutionSettings;
import com.crud.rest.service.FitnesseSuiteService;
import com.crud.rest.service.SuiteExecutionServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fitnesse.components.RMIInterface;

//@Component
public class ScheduledTasks {

	//private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	private SuiteExecutionServiceImpl suiteExecutionService;

	@Autowired
	private FitnesseSuiteService fitnesseSuiteService;

	private boolean isAnySuiteAlreadyRunning, scheduledExecution = true;

	private String resultPublishServerAddress;

	public ScheduledTasks(SuiteExecutionServiceImpl suiteExecutionService, FitnesseSuiteService fitnesseSuiteService,
			boolean scheduledExecution) {
		this.suiteExecutionService = suiteExecutionService;
		this.fitnesseSuiteService = fitnesseSuiteService;
		this.scheduledExecution = scheduledExecution;
	}

	public ScheduledTasks(String resultPublishServerAddress) {
		this.resultPublishServerAddress = resultPublishServerAddress;
	}

	// @Scheduled(fixedRate=60000)
	public void triggerTestExecution(String fitnesseUsername, String fitnessePassword, boolean forcedExecution) {
		CustomLogger.logInfo("Test execution started at " + dateFormat.format(new Date()));

		List<FitnesseSuite> fitnesseSuites = fitnesseSuiteService.findAllSuites();
		fitnesseSuites.removeIf(eachSuite -> !eachSuite.getShouldRun());

		// For forced execution we don't check the status of previous execution.
		if (!forcedExecution) {
			// If any of the suite is already running, don't run again. Abort the whole run for all suites. We can change the logic in future.
			isAnySuiteAlreadyRunning = false;
			fitnesseSuites.forEach((eachSuite) -> {
				if (eachSuite.isRunning()) {
					CustomLogger.logError(String.format("%s suite is still running. Cannot run again. Aborting.",
							eachSuite.getSuiteName()));
					isAnySuiteAlreadyRunning = true;
				}
			});

			if (isAnySuiteAlreadyRunning)
				return;
		}

		TestExecutionSettings testExecutionsettings = suiteExecutionService.getCurrentSettings();
		if (scheduledExecution) {
			Calendar date = Calendar.getInstance();
			long currentTime = date.getTimeInMillis();
			Date afterAddingIntervalInMinutes = new Date(
					currentTime + (testExecutionsettings.getExecutionInterval() * 60000));
			testExecutionsettings.setNextExecutionTime(afterAddingIntervalInMinutes);
		}
		String decryptedFitnessePassword = null;
		if (fitnessePassword != null && fitnessePassword.length() > 1) {
			StandardPBEStringEncryptor encryptorDecryptor = new StandardPBEStringEncryptor();
			encryptorDecryptor.setPassword(AppConfig.encryptionSeed);
			decryptedFitnessePassword = encryptorDecryptor.decrypt(fitnessePassword);
		}

		testExecutionsettings.setRunning(true);
		suiteExecutionService.updateTestExecutionSettings(testExecutionsettings);

		CustomLogger.logInfo("Starting intermediate result listener thread...");
		try {
			Thread rmiThread = new Thread(new RMIListener(fitnesseSuites));
			rmiThread.start();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		CustomLogger.logInfo("Intermediate result listener thread started...");

		ExecutorService executor = Executors.newFixedThreadPool(testExecutionsettings.getNumberOfExecutionThread());

		for (FitnesseSuite fitnesseSuite : fitnesseSuites)
			executor.submit(new TestExecution(fitnesseSuite, fitnesseUsername, decryptedFitnessePassword));

		executor.shutdown();

		CustomLogger.logInfo(String.format(new Date() + " : Running %d suites in parallel...", fitnesseSuites.size()));

		try {
			executor.awaitTermination(testExecutionsettings.getConnectionTimeOutInMinutes(), TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		testExecutionsettings.setRunning(false);
		suiteExecutionService.updateTestExecutionSettings(testExecutionsettings);

		try {
			Naming.unbind(resultPublishServerAddress);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			e.printStackTrace();
		}

		CustomLogger.logInfo(new Date() + ": Execution completed. Check log file for the details.");
	}

	private class RMIListener extends UnicastRemoteObject implements RMIInterface, Runnable {
		private List<FitnesseSuite> fitnesseSuites;
		FitnesseSuite matchingSuite;

		private static final long serialVersionUID = 1L;

		public RMIListener(List<FitnesseSuite> fitnesseSuites) throws RemoteException {
			super();
			this.fitnesseSuites = fitnesseSuites;
		}

		public String publish(String text) throws RemoteException {

			JsonNode rootNode = null;
			try {
				rootNode = new ObjectMapper().readTree(new StringReader(text));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			int right = rootNode.get("right").asInt();
			int wrong = rootNode.get("wrong").asInt();
			int ignores = rootNode.get("ignores").asInt();
			int exceptions = rootNode.get("exceptions").asInt();
			String testName = rootNode.get("testName").asText();
			String testPath = rootNode.get("testPath").asText();

			fitnesseSuites.forEach((suite) -> {
				try {
					String fullUrlString = suite.getSuiteUrl();
					URL urlConvertedToURLVariable = new URL(fullUrlString);
					String theFitnessePathFromDatabase = urlConvertedToURLVariable.getPath().replaceAll("/", "");

					if (testPath.contains(theFitnessePathFromDatabase)) {
						matchingSuite = suite;
						return;
					}
				} catch (MalformedURLException e) {
					CustomLogger.logError(e.toString());
				}
			});

			int assertionFailures = 0;
			if (right > 0)
				assertionFailures = wrong + exceptions;
			else
				assertionFailures = wrong + ignores + exceptions;

			TestExecutionSettings testExecutionsettings = suiteExecutionService.getCurrentSettings();
			if (testExecutionsettings.isQueuePolling())
				suiteExecutionService.updateResultDatabase(matchingSuite.getSuiteId(), testName,
						assertionFailures > 0 ? "FAILED" : "PASSED");

			CustomLogger.logInfo(text);
			return text;
		}

		@Override
		public void run() {
			try {
				Naming.bind(resultPublishServerAddress, new RMIListener(fitnesseSuites));
				CustomLogger.logInfo("Result publisher is ready.");
			} catch (Exception e) {
				CustomLogger.logInfo("Result Publisher server exception: " + e.toString());
				e.printStackTrace();
			}
		}

	}

	private class TestExecution implements Runnable {

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
				CustomLogger.logInfo(fitnesseSuite.getSuiteName() + " threw an error.\n" + e);
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
