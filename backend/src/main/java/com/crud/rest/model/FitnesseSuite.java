package com.crud.rest.model;

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

	// constructor with fields
	public FitnesseSuite(int suiteId, String suiteName, String suiteUrl, boolean shouldRun) {
		super();
		this.suiteId = suiteId;
		this.suiteName = suiteName;
		this.suiteUrl = suiteUrl;
		this.shouldRun = shouldRun;
	}

	// constructor without fields
	public FitnesseSuite() {
		super();
	}

}
