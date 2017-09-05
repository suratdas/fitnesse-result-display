package com.crud.rest.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.service.FitnesseSuiteService;
import com.crud.rest.service.SuiteExecutionServiceImpl;

@RestController // combination of @Controller and @ResponseBody annotations
@RequestMapping("fitnesse")
public class SuiteController {

	@Autowired
	@Qualifier("fitnesseSuiteServiceImpl")
	// @Qualifier("duplicateServiceImpl")
	private FitnesseSuiteService fitnesseService;

	@Autowired
	private SuiteExecutionServiceImpl suiteExecutionServiceImpl;

	/*
	 * public void setFitnesseService(FitnesseSuiteService fitnesseService) {
	 * this.fitnesseService = fitnesseService; }
	 */
	@RequestMapping(value = "/suite/new", method = RequestMethod.POST)
	public ResponseEntity<Void> addFitnesseSuite(@RequestBody FitnesseSuite fitnesse,
			@RequestHeader("Accept") String header, UriComponentsBuilder ucb) {

		if (fitnesseService.isSuiteExist(fitnesse)) {
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		} else {

			fitnesseService.saveSuite(fitnesse);
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(ucb.path("/fitnesse/{id}").buildAndExpand(fitnesse.getSuiteId()).toUri());
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		}
	}

	@RequestMapping(value = "/suite/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FitnesseSuite> getSuite(@PathVariable("id") String id) {

		FitnesseSuite fitnesse = fitnesseService.findBySuiteId(id);
		if (fitnesse == null) {
			return new ResponseEntity<FitnesseSuite>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<FitnesseSuite>(fitnesse, HttpStatus.OK);
	}

	@RequestMapping(value = "/suites", method = RequestMethod.GET)
	public ResponseEntity<List<FitnesseSuite>> listAllSuites() {
		List<FitnesseSuite> fitnesses = fitnesseService.findAllSuites();
		if (fitnesses.isEmpty()) {
			return new ResponseEntity<List<FitnesseSuite>>(HttpStatus.NO_CONTENT);
		}
		fitnesses.forEach(f -> System.out.println(f.getSuiteName()));
		return new ResponseEntity<List<FitnesseSuite>>(fitnesses, HttpStatus.OK);
	}

	@RequestMapping(value = "/suite/{id}", method = RequestMethod.PUT)
	public ResponseEntity<FitnesseSuite> updateSuite(@PathVariable("id") String id,
			@RequestBody FitnesseSuite fitnesseSuitePassed) {

		FitnesseSuite fitnesseSuite = fitnesseService.findBySuiteId(id);

		if (fitnesseSuitePassed == null) {
			return new ResponseEntity<FitnesseSuite>(HttpStatus.NOT_FOUND);
		}

		fitnesseSuite.setSuiteName(fitnesseSuitePassed.getSuiteName());
		fitnesseSuite.setSuiteUrl(fitnesseSuitePassed.getSuiteUrl());
		fitnesseSuite.setShouldRun(fitnesseSuitePassed.getShouldRun());
		fitnesseSuite.setSuiteId(fitnesseSuitePassed.getSuiteId());

		fitnesseService.updateSuite(fitnesseSuitePassed);
		return new ResponseEntity<FitnesseSuite>(fitnesseSuite, HttpStatus.OK);
	}

	@RequestMapping(value = "/suite/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<FitnesseSuite> deleteSuite(@PathVariable("id") int id) {

		FitnesseSuite fitnesse = fitnesseService.findBySuiteId(Integer.toString(id));
		if (fitnesse == null) {
			return new ResponseEntity<FitnesseSuite>(HttpStatus.NOT_FOUND);
		}

		fitnesseService.deleteSuiteById(id);
		suiteExecutionServiceImpl.deleteAllResults(id);
		return new ResponseEntity<FitnesseSuite>(HttpStatus.NO_CONTENT);
	}

	// This is risky as it deletes everything from database.
	@RequestMapping(value = "/suite/deleteAll", method = RequestMethod.DELETE)
	public ResponseEntity<FitnesseSuite> deleteAllSuites() {

		fitnesseService.deleteAllSuites();
		suiteExecutionServiceImpl.deleteAllResults();
		return new ResponseEntity<FitnesseSuite>(HttpStatus.NO_CONTENT);
	}

}
