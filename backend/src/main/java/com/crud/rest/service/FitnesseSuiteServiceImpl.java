package com.crud.rest.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crud.rest.dao.FitnesseSuiteDao;
import com.crud.rest.model.FitnesseSuite;

@Service
public class FitnesseSuiteServiceImpl implements FitnesseSuiteService {

	@Autowired
	private FitnesseSuiteDao fitnesseDao;

	/*
	 * public void setFitnesseDao(FitnesseSuiteDao fitnesseDao) {
	 * this.fitnesseDao = fitnesseDao; }
	 */

	@Override
	public FitnesseSuite findById(long id) {
		return fitnesseDao.findById(id);
	}

	@Override
	public FitnesseSuite findBySuiteId(String id) {
		return fitnesseDao.findBySuiteId(id);
	}

	@Override
	public FitnesseSuite findBySuiteName(String name) {
		return fitnesseDao.findBySuiteName(name);
	}

	@Override
	public void saveSuite(FitnesseSuite fitnesse) {
		fitnesseDao.saveSuite(fitnesse);
	}

	@Override
	public void updateSuite(FitnesseSuite fitnesse) {
		fitnesseDao.updateSuite(fitnesse);
	}

	@Override
	public void deleteSuiteById(int id) {
		fitnesseDao.deleteSuiteById(id);
	}

	@Override
	public List<FitnesseSuite> findAllSuites() {
		return fitnesseDao.findAllSuites();
	}

	@Override
	public void deleteAllSuites() {
		fitnesseDao.deleteAllSuites();
	}

	@Override
	public boolean isSuiteExist(FitnesseSuite fitnesseSuite) {
		return fitnesseDao.isSuiteExist(fitnesseSuite);
	}

	@Override
	public void updateTestSuite(FitnesseSuite fitnesseSuite) {
		fitnesseDao.updateTestSuite(fitnesseSuite);
	}

}