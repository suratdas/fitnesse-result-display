package com.crud.rest.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ContextLoader;

import com.crud.rest.configuration.AppConfig;
import com.crud.rest.configuration.CustomLogger;
import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.model.TestExecutionSettings;
import com.crud.rest.service.FitnesseSuiteService;
import com.crud.rest.service.SuiteExecutionServiceImpl;

@RestController
@RequestMapping("execution")
public class ExecutionController {

	@Autowired
	private SuiteExecutionServiceImpl suiteExecutionService;

	@Autowired
	private FitnesseSuiteService fitnesseSuiteService;

	@RequestMapping(value = "/changePollingInterval/{interval}", method = RequestMethod.GET)
	public String changePollingInterval(@PathVariable("interval") int interval) {
		suiteExecutionService.setPollingInterval(interval);
		return "Updated to " + interval + " minute(s).";
	}

	@RequestMapping(value = "/changeExecutionTime/{nextExecutionTime}", method = RequestMethod.GET)
	public String changeExecutionTime(@PathVariable("nextExecutionTime") String nextExecutionTime) {
		try {
			if (nextExecutionTime.length() < 0)
				throw new Exception();
			Date nextExecutionTimeInDateFormat;
			nextExecutionTimeInDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(nextExecutionTime);
			suiteExecutionService.setNextExecutionTime(nextExecutionTimeInDateFormat);
			return "Changed to " + nextExecutionTime + ".";
		} catch (Exception e) {
			return "Please send the request in the format : 2017-09-11%2022:16:45";
		}
	}

	@RequestMapping(value = "/start", method = RequestMethod.GET)
	public String triggerAnExecution() {
		return triggerExecution(false);
	}

	@RequestMapping(value = "/forceStart", method = RequestMethod.GET)
	public String triggerForcedExecution() {
		return triggerExecution(true);
	}

	private String triggerExecution(boolean forcedExecution) {
		//AppConfig config = appContext.getBean(AppConfig.class); //To use this, we have to "Autowire" private ApplicationContext appContext;
		AppConfig config = ContextLoader.getCurrentWebApplicationContext().getBean(AppConfig.class);
		TestExecutionSettings testExecutionSettings = suiteExecutionService.getCurrentSettings();
		config.myBean().triggerTestExecution(testExecutionSettings.getFitnesseUserName(),
				testExecutionSettings.getFitnessePassword(), forcedExecution);
		return "Execution done";
	}

	@RequestMapping(value = "/resetRunningStatusForAllSuites", method = RequestMethod.GET)
	public String enableAllSuites() {
		List<FitnesseSuite> suites = fitnesseSuiteService.findAllSuites();
		suites.forEach(suite -> {
			if (suite.isRunning()) {
				suite.setRunning(false);
				fitnesseSuiteService.updateSuite(suite);
				CustomLogger.logInfo(suite.getSuiteName() + " running status reset.");
			}
		});
		TestExecutionSettings settings = suiteExecutionService.getCurrentSettings();

		if (settings.isRunning()) {
			settings.setRunning(false);
			suiteExecutionService.updateTestExecutionSettings(settings);
			CustomLogger.logInfo("Execution status reset.");
		}
		return "All suites reset.";
	}

	@RequestMapping(value = "/encrypt/{value}", method = RequestMethod.GET)
	public String encryptPassword(@PathVariable String value) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(AppConfig.encryptionSeed);
		return encryptor.encrypt(value);

	}

	@RequestMapping(value = "/decrypt/{value}", method = RequestMethod.GET)
	public String decryptPassword(@PathVariable String value) {
		StandardPBEStringEncryptor encryptorDecryptor = new StandardPBEStringEncryptor();
		encryptorDecryptor.setPassword(AppConfig.encryptionSeed);
		return encryptorDecryptor.decrypt(value);

	}

}
