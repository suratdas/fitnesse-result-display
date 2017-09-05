package com.crud.rest.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "test_suites")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class FitnesseSuite {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@Column(name = "suite_id")
	private int suiteId;

	@Column(name = "suite_name")
	private String suiteName;

	@Column(name = "suite_url")
	private String suiteUrl;

	@Column(name = "should_run")
	private boolean shouldRun;

	@Column(name = "is_running")
	private boolean isRunning;
	
	@Column(name="total_tests")
	private int totalTests;

	@Column(name="passed_tests")
	private int passedTests;

	@Column(name="failed_tests")
	private int failedTests;
	
	@Column(name="last_execution_time")
	private Date lastExecutionTime;

	@Column(name="next_execution_time")
	private Date nextExecutionTime;

	// setters and getters
	public int getSuiteId() {
		return suiteId;
	}

	public void setSuiteId(int id) {
		this.suiteId = id;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String name) {
		this.suiteName = name;
	}

	public String getSuiteUrl() {
		return suiteUrl;
	}

	public void setSuiteUrl(String suiteUrl) {
		this.suiteUrl = suiteUrl;
	}

	public boolean getShouldRun() {
		return shouldRun;
	}

	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	public int getTotalTests() {
		return totalTests;
	}

	public void setTotalTests(int totalTests) {
		this.totalTests = totalTests;
	}

	public int getPassedTests() {
		return passedTests;
	}

	public void setPassedTests(int passedTests) {
		this.passedTests = passedTests;
	}

	public int getFailedTests() {
		return failedTests;
	}

	public void setFailedTests(int failedTests) {
		this.failedTests = failedTests;
	}

	public Date getLastExecutionTime() {
		return lastExecutionTime;
	}

	public void setLastExecutionTime(Date lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

	public Date getNextExecutionTime() {
		return nextExecutionTime;
	}

	public void setNextExecutionTime(Date nextExecutionTime) {
		this.nextExecutionTime = nextExecutionTime;
	}
	

	// constructor with fields
	public FitnesseSuite(int suiteId, String suiteName, String suiteUrl, boolean shouldRun, boolean isRunning,
			int totalTests, int passedTests, int failedTests, Date lastExecutionTime, Date nextExecutionTime) {
		super();
		this.suiteId = suiteId;
		this.suiteName = suiteName;
		this.suiteUrl = suiteUrl;
		this.shouldRun = shouldRun;
		this.isRunning = isRunning;
		this.totalTests = totalTests;
		this.passedTests = passedTests;
		this.failedTests = failedTests;
		this.lastExecutionTime = lastExecutionTime;
		this.nextExecutionTime = nextExecutionTime;
	}

	/*public FitnesseSuites(int suiteId, String suiteName, String suiteUrl, boolean shouldRun, boolean isRunning) {
		super();
		this.suiteId = suiteId;
		this.suiteName = suiteName;
		this.suiteUrl = suiteUrl;
		this.shouldRun = shouldRun;
		this.isRunning = isRunning;
	}*/

	// constructor without fields
	public FitnesseSuite() {
		super();
	}

}
