package com.crud.rest.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crud.rest.dao.TestCaseResultDao;
import com.crud.rest.dao.TestExecutionSettingsDao;
import com.crud.rest.model.AllTestResult;
import com.crud.rest.model.TestExecutionSettings;

@Service
public class SuiteExecutionServiceImpl {

	public enum TestResultType {
		Passed, Failed
	}

	@Autowired
	private TestCaseResultDao testCaseResultDao;

	@Autowired
	private TestExecutionSettingsDao testExecutionSettingsDao;

	public void executeSuite(int suiteId, String suiteURL, String fitnesseUsername, String fitnessePassword) {
		testCaseResultDao.clearPreviousTestResultsForSuite(suiteId);
		runFitnesseSuite(suiteId, suiteURL, fitnesseUsername, fitnessePassword);
		testCaseResultDao.deleteUnusedTestResults(suiteId);
	}

	public void deleteAllResults(int suiteId) {
		testCaseResultDao.deleteResultsForSuite(suiteId);
	}

	public void deleteAllResults() {
		testCaseResultDao.deleteAllResults();
	}

	private void runFitnesseSuite(int suiteId, String fitnessesuiteURL, String fitnesseUsername,
			String fitnessePassword) {
		try {

			URL url = new URL(fitnessesuiteURL + "?suite&format=xml");

			URLConnection con = url.openConnection();
			// Authorization is not used if username is not provided (=null or empty)
			if (fitnesseUsername != null && fitnesseUsername.trim().length() > 1) {
				String authString = fitnesseUsername + ":" + fitnessePassword;
				String authStringEnc = Base64.getEncoder().encodeToString(authString.getBytes("UTF-8"));
				con.setRequestProperty("Authorization", "Basic " + authStringEnc);
			}
			int timeout = findCurrentSettings().getConnectionTimeOut();
			con.setReadTimeout(timeout);
			// TODO Find out if it is possible to add result to database when the execution is on.
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			String oneResultNode = "";
			boolean collateNode = false;
			while ((inputLine = in.readLine()) != null) {
				if (collateNode) {
					oneResultNode += inputLine + "\n";
					if (inputLine.trim().startsWith("</result")) {
						processEachTestCase(suiteId, oneResultNode);
						oneResultNode = "";
						collateNode = false;
					}
				}
				if (inputLine.trim().startsWith("<result")) {
					oneResultNode += inputLine + "\n";
					collateNode = true;
				}
			}
			System.out.println(oneResultNode);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processEachTestCase(int suiteId, String oneResultNode) {
		try {
			DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = newDocumentBuilder.parse(new ByteArrayInputStream(oneResultNode.getBytes()));

			NodeList nodeNames = doc.getElementsByTagName("relativePageName");
			Node node = nodeNames.item(0);

			// If getTextContent is not recognized, go to project properties and set the JRE to top and select it.
			String testName = node.getTextContent();

			int right = Integer.parseInt(doc.getElementsByTagName("right").item(0).getTextContent().trim());
			int wrong = Integer.parseInt(doc.getElementsByTagName("wrong").item(0).getTextContent().trim());
			int ignores = Integer.parseInt(doc.getElementsByTagName("ignores").item(0).getTextContent().trim());
			int exceptions = Integer.parseInt(doc.getElementsByTagName("exceptions").item(0).getTextContent().trim());

			int assertionFailures = 0;
			if (right > 0)
				assertionFailures = wrong + exceptions;
			else
				assertionFailures = wrong + ignores + exceptions;

			updateResultDatabase(suiteId, testName, assertionFailures > 0 ? "FAILED" : "PASSED");

		} catch (SAXException | IOException | ParserConfigurationException | FactoryConfigurationError e) {
			e.printStackTrace();
		}
	}

	private void updateResultDatabase(int suiteId, String testName, String status) {
		AllTestResult allTestCaseResult = testCaseResultDao.findTestCase(suiteId, testName);

		if (allTestCaseResult == null) {
			allTestCaseResult = new AllTestResult(suiteId, testName, status);
			allTestCaseResult.setLastExecutionTime(new Date(System.currentTimeMillis()));
			testCaseResultDao.createTestCaseResult(allTestCaseResult);
		} else {
			allTestCaseResult.setStatus(status);
			testCaseResultDao.updateTestCaseResult(allTestCaseResult);
		}
	}

	public int getFailedTestCaseCount(int suiteId) {
		return getTestCaseCount(suiteId, TestResultType.Failed);
	}

	public int getPassedTestCaseCount(int suiteId) {
		return getTestCaseCount(suiteId, TestResultType.Passed);
	}

	private int getTestCaseCount(int suiteId, TestResultType type) {
		if (type == TestResultType.Passed)
			return testCaseResultDao.getTestCaseCount(suiteId, "PASSED");
		else if (type == TestResultType.Failed)
			return testCaseResultDao.getTestCaseCount(suiteId, "FAILED");
		return 0;
	}

	public TestExecutionSettings findCurrentSettings() {
		return testExecutionSettingsDao.getCurrentSettings();
	}

	public int findPollingIntervalInMinutes() {
		return testExecutionSettingsDao.findPollingIntervalInMinutes();
	}

	public void setPollingInterval(int settings) {
		testExecutionSettingsDao.setPollingInterval(settings);
	}

	public void setNextExecutionTime(Date date) {
		testExecutionSettingsDao.setNextExecutionTime(date);
	}

	public void updateTestExecutionSettings(TestExecutionSettings testExecutionsettings) {
		testExecutionSettingsDao.updateTestExecutionSettings(testExecutionsettings);
		
	}

}
