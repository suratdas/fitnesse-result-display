package com.crud.rest.dao;

import com.crud.rest.dao.TestCaseResultsDaoImpl.DeleteType;
import com.crud.rest.model.AllTestResult;

public interface TestCaseResultDao {

	void updateTestCaseResult(AllTestResult fitnesseTestCaseResult);

	AllTestResult findTestCase(int suiteId, String testName);

	void createTestCaseResult(AllTestResult fitnesseTestCaseResult);

	void clearAllTestResult(int suiteId);

	void deleteTestCaseResults(int suiteId, DeleteType deleteType);

}
