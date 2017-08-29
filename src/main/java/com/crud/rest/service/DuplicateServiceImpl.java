package com.crud.rest.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.crud.rest.model.FitnesseSuite;

@Service
public class DuplicateServiceImpl implements FitnesseSuiteService {

	@Override
	public FitnesseSuite findById(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FitnesseSuite findBySuiteName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveSuite(FitnesseSuite Customer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSuite(FitnesseSuite Customer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteSuiteById(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<FitnesseSuite> findAllSuites() {
		// TODO Auto-generated method stub
		System.out.println("In duplicate implementation.");
		return null;
	}

	@Override
	public void deleteAllSuites() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSuiteExist(FitnesseSuite Customer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FitnesseSuite findBySuiteId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
