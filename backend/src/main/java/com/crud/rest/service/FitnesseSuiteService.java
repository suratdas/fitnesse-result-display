package com.crud.rest.service;

import java.util.List;

import com.crud.rest.model.FitnesseSuite;

public interface FitnesseSuiteService {

	FitnesseSuite findById(long id);

	FitnesseSuite findBySuiteName(String name);

	void saveSuite(FitnesseSuite Customer);

	void updateSuite(FitnesseSuite Customer);

	void deleteSuiteById(int id);

	List<FitnesseSuite> findAllSuites();

	void deleteAllSuites();

	boolean isSuiteExist(FitnesseSuite Customer);

	FitnesseSuite findBySuiteId(String id);

	void markTestRunning(FitnesseSuite fitnesseSuite, boolean isRunning);
}
