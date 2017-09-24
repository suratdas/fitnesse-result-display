package com.crud.rest.configuration;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.model.TestExecutionSettings;
import com.crud.rest.service.FitnesseSuiteService;
import com.crud.rest.service.SuiteExecutionServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//@Component
public class ScheduledTasks {

	//private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

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
		Thread queueThread = new Thread(new JMSQueueListener(fitnesseSuites));
		queueThread.start();
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

		CustomLogger.logInfo(new Date() + ": Execution completed. Check log file for the details.");
	}

	//It uses ActiveMQ server
	private class JMSQueueListener implements Runnable {
		private List<FitnesseSuite> fitnesseSuites;
		FitnesseSuite matchingSuite;

		public JMSQueueListener(List<FitnesseSuite> fitnesseSuites) {
			this.fitnesseSuites = fitnesseSuites;
		}

		@Override
		public void run() {
			try {
				TestExecutionSettings testExecutionsettings = suiteExecutionService.getCurrentSettings();
				CustomLogger.logInfo("Listening to the queue...");
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
						testExecutionsettings.getMessageBrokerAddress());
				Connection connection = connectionFactory.createConnection();
				connection.start();
				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				Destination destination = session.createQueue(testExecutionsettings.getMessageQueueName());
				MessageConsumer consumer = session.createConsumer(destination);
				
				while (testExecutionsettings.isRunning()) {
					Message message = consumer.receive(60000);

					if (message instanceof TextMessage) {
						TextMessage textMessage = (TextMessage) message;
						String text = textMessage.getText();
						System.out.println("Received message: " + text);
						JsonNode rootNode = new ObjectMapper().readTree(new StringReader(text));
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
								String theFitnessePathFromDatabase = urlConvertedToURLVariable.getPath().replaceAll("/",
										"");

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
						
						if (testExecutionsettings.isQueuePolling())
							suiteExecutionService.updateResultDatabase(matchingSuite.getSuiteId(), testName,
									assertionFailures > 0 ? "FAILED" : "PASSED");

						CustomLogger.logInfo(text);
					} else
						CustomLogger.logInfo(
								"Either there was no message in the queue or the message cannot be converted to text.");
					testExecutionsettings = suiteExecutionService.getCurrentSettings();
				}
				CustomLogger.logInfo("Stopped listening to the queue.");
				consumer.close();
				session.close();
				connection.close();
			} catch (Exception e) {
				CustomLogger.logError(e.toString());
			} finally {

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
