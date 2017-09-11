package com.crud.rest.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.crud.rest.configuration.ScheduledTasks;
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
		return "Updated to " + interval + " minutes.";
	}

	@RequestMapping(value = "/changeExecutionTime/{nextExecutionTime}", method = RequestMethod.GET)
	public String changeExecutionTime(@PathVariable("nextExecutionTime") String nextExecutionTime) {
		if (nextExecutionTime.length() < 0)
			return "Please send the request in the format : 2017-09-11%2017:16:45";
		try {
			Date nextExecutionTimeInDateFormat;
			nextExecutionTimeInDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(nextExecutionTime);
			suiteExecutionService.setNextExecutionTime(nextExecutionTimeInDateFormat);
			return "Changed to " + nextExecutionTime + ".";
		} catch (ParseException e) {
			return "Please send the request in the format : 2017-09-11%2022:16:45";
		}
	}

	@RequestMapping(value = "/start", method = RequestMethod.GET)
	public void triggerAnExecution() {
		ScheduledTasks execution = new ScheduledTasks(suiteExecutionService, fitnesseSuiteService, false);
		TestExecutionSettings testExecutionSettings = suiteExecutionService.findCurrentSettings();
		execution.triggerTestExecution(testExecutionSettings.getFitnesseUserName(),
				testExecutionSettings.getFitnessePassword());
	}

}
