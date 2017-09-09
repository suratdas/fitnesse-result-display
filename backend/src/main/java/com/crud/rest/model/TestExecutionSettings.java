package com.crud.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "execution_settings")
//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TestExecutionSettings {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@Column(name = "execution_thread")
	private int numberOfExecutionThread;

	@Column(name = "fitnesse_username")
	private String fitnesseUserName;

	@Column(name = "fitnesse_password")
	private String fitnessePassword;

	@Column(name = "connection_timeout")
	private int connectionTimeOut;

	@Column(name = "execution_interval")
	private int executionInterval;

	@Column(name = "last_execution_time")
	private String lastExecutionTime;

	public int getNumberOfExecutionThread() {
		return numberOfExecutionThread;
	}

	public void setNumberOfExecutionThread(int numberOfExecutionThread) {
		this.numberOfExecutionThread = numberOfExecutionThread;
	}

	public String getFitnesseUserName() {
		return fitnesseUserName;
	}

	public void setFitnesseUserName(String fitnesseUserName) {
		this.fitnesseUserName = fitnesseUserName;
	}

	public String getFitnessePassword() {
		return fitnessePassword;
	}

	public void setFitnessePassword(String fitnessePassword) {
		this.fitnessePassword = fitnessePassword;
	}

	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}

	public int getExecutionInterval() {
		return executionInterval;
	}

	public void setExecutionInterval(int executionInterval) {
		this.executionInterval = executionInterval;
	}

	public String getLastExecutionTime() {
		return lastExecutionTime;
	}

	public void setLastExecutionTime(String lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}
}
