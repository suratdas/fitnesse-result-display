package com.crud.rest.service;

import java.util.List;

import com.crud.rest.model.FitnesseSuite;

public interface FitnesseSuiteService {

	FitnesseSuite findById(long id);

	FitnesseSuite findBySuiteName(String name);

	void saveSuite(FitnesseSuite fitnesseSuite);

	void updateSuite(FitnesseSuite fitnesseSuite);

	void deleteSuiteById(int id);

	List<FitnesseSuite> findAllSuites();

	void deleteAllSuites();

	boolean isSuiteExist(FitnesseSuite fitnesseSuite);

	FitnesseSuite findBySuiteId(String id);

	void updateTestSuite(FitnesseSuite fitnesseSuite);
}
