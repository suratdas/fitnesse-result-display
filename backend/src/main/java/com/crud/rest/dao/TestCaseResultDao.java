package com.crud.rest.dao;

import com.crud.rest.model.AllTestResult;

public interface TestCaseResultDao {

	void updateTestCaseResult(AllTestResult fitnesseTestCaseResult);

	AllTestResult findTestCase(int suiteId, String testName);

	void createTestCaseResult(AllTestResult fitnesseTestCaseResult);

	void clearPreviousTestResultsForSuite(int suiteId);

	int getTestCaseCount(int suiteId, String string);

	void deleteAllResults();

	void deleteResultsForSuite(int suiteId);

	void deleteUnusedTestResults(int suiteId);

}
