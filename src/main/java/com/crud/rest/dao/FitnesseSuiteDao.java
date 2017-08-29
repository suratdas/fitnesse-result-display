package com.crud.rest.dao;

import java.util.List;

import com.crud.rest.model.FitnesseSuite;

public interface FitnesseSuiteDao {

	FitnesseSuite findBySuiteId(String id);

	FitnesseSuite findBySuiteName(String name);

	void saveSuite(FitnesseSuite Customer);

	void updateSuite(FitnesseSuite Customer);

	void deleteSuiteById(int id);

	List<FitnesseSuite> findAllSuites();

	void deleteAllSuites();

	boolean isSuiteExist(FitnesseSuite Customer);

	FitnesseSuite findById(long id);
}
