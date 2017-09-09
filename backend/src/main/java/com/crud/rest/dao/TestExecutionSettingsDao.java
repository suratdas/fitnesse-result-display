package com.crud.rest.dao;

import com.crud.rest.model.TestExecutionSettings;

public interface TestExecutionSettingsDao {

	int findIntervalBetweenExecutionTimeInSeconds();

	void setExecutionInterval(int settings);

	TestExecutionSettings getCurrentSettings();

	void setLastExecutionTime(String date);

}
