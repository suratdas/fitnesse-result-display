package com.crud.rest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "detail_result")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AllTestResult {

	@Id
	@GeneratedValue
	@Column
	private long id;

	//int(4) with suite value corresponds to that defined in FitnesseSuite.java
	@Column(name = "suite_id")
	private int suiteId;

	//varchar(45)
	@Column(name = "test_name")
	private String testName;

	//varchar(45)
	@Column(name = "status")
	private String status;

	//datetime
	@Column(name = "last_execution_time")
	private Date lastExecutionTime;

	public int getSuiteId() {
		return suiteId;
	}

	public void setSuiteId(int suiteId) {
		this.suiteId = suiteId;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastExecutionTime() {
		return lastExecutionTime;
	}

	public void setLastExecutionTime(Date lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

	public AllTestResult(int suiteId, String testName, String status) {
		super();
		this.suiteId = suiteId;
		this.testName = testName;
		this.status = status;
	}

	public AllTestResult() {
		super();
	}

}
