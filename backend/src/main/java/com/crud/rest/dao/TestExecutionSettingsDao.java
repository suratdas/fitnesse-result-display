package com.crud.rest.dao;

import java.util.Date;

import com.crud.rest.model.TestExecutionSettings;

public interface TestExecutionSettingsDao {

	TestExecutionSettings getCurrentSettings();

	int getPollingIntervalInMinutes();

	void setPollingInterval(int settings);

	void setNextExecutionTime(Date date);

	void updateTestExecutionSettings(TestExecutionSettings settings);

}
