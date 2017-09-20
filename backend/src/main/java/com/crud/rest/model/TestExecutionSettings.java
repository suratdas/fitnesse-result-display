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

	//varchar(45)
	@Column(name = "fitnesse_username")
	private String fitnesseUserName;

	//varchar(100)  => encrypted password
	@Column(name = "fitnesse_password")
	private String fitnessePassword;

	//int(11) => put 720 for 12 hours
	@Column(name = "connection_timeout_in_minutes")
	private int connectionTimeOutInMinutes;

	//int(11) => put 1 to poll every minute
	@Column(name = "polling_interval_in_minutes")
	private int pollingInterval;

	//int(11) => put 1440 for every 24 hours
	@Column(name = "execution_interval_in_minutes")
	private int executionInterval;

	//datetime => can be null too.
	@Column(name = "next_execution_time", columnDefinition = "DATETIME")
	private Date nextExecutionTime;

	//bit(1)
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

	public int getConnectionTimeOutInMinutes() {
		return connectionTimeOutInMinutes;
	}

	public void setConnectionTimeOutInMinutes(int connectionTimeOut) {
		this.connectionTimeOutInMinutes = connectionTimeOut;
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
