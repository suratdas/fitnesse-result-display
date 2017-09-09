package com.crud.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.crud.rest.model.TestExecutionSettings;
import com.crud.rest.service.SuiteExecutionServiceImpl;

@RestController
@RequestMapping("execution")
public class ExecutionController {

	@Autowired
	private SuiteExecutionServiceImpl suiteExecutionService;

	@RequestMapping(value = "/changeinterval", method = RequestMethod.POST)
	public void changeNextExecutionTime(@RequestBody TestExecutionSettings settings) {
		suiteExecutionService.setExecutionInterval(settings.getExecutionInterval());
	}

}
