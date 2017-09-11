package com.crud.rest.model;

import java.util.Date;

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

	@Column(name = "polling_interval_in_minutes")
	private int pollingInterval;

	@Column(name = "execution_interval_in_minutes")
	private int executionInterval;

	@Column(name = "next_execution_time", columnDefinition = "DATETIME")
	private Date nextExecutionTime;

	@Column(name = "is_running")
	private boolean isRunning;

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

	public int getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(int pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public Date getNextExecutionTime() {
		return nextExecutionTime;
	}

	public void setNextExecutionTime(Date nextExecutionTime) {
		this.nextExecutionTime = nextExecutionTime;
	}

	public int getExecutionInterval() {
		return executionInterval;
	}

	public void setExecutionInterval(int executionInterval) {
		this.executionInterval = executionInterval;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

}
