package com.crud.rest.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crud.rest.configuration.CustomLogger;
import com.crud.rest.dao.TestCaseResultDao;
import com.crud.rest.dao.TestExecutionSettingsDao;
import com.crud.rest.model.AllTestResult;
import com.crud.rest.model.FitnesseSuite;
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

	public void executeSuite(FitnesseSuite fitnesseSuite, String fitnesseUsername, String fitnessePassword)
			throws Exception {
		int suiteId = fitnesseSuite.getSuiteId();
		testCaseResultDao.clearPreviousTestResultsForSuite(suiteId);
		runFitnesseSuite(fitnesseSuite, fitnesseUsername, fitnessePassword);
		testCaseResultDao.deleteUnusedTestResults(suiteId);
	}

	public void deleteAllResults(int suiteId) {
		testCaseResultDao.deleteResultsForSuite(suiteId);
	}

	public void deleteAllResults() {
		testCaseResultDao.deleteAllResults();
	}

	private void runFitnesseSuite(FitnesseSuite fitnesseSuite, String fitnesseUsername, String fitnessePassword)
			throws Exception {

		int suiteId = fitnesseSuite.getSuiteId();

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			HttpGet httpGet = new HttpGet(fitnesseSuite.getSuiteUrl() + "?suite&publish&format=xml");

			// Authorization is not used if username is not provided (=null or empty)
			if (fitnesseUsername != null && fitnesseUsername.trim().length() > 1) {
				UsernamePasswordCredentials creds = new UsernamePasswordCredentials(fitnesseUsername, fitnessePassword);
				Header headerAuth = new BasicScheme(StandardCharsets.UTF_8).authenticate(creds, httpGet, null);
				httpGet.addHeader(headerAuth);
			}

			String timeout = Integer
					.toString(testExecutionSettingsDao.getCurrentSettings().getConnectionTimeOutInMinutes() * 60);
			Header headerTimeout = new BasicHeader(HttpHeaders.TIMEOUT, timeout);
			Header headerconnection = new BasicHeader(HttpHeaders.CONNECTION, "Keep-alive");

			httpGet.addHeader(headerTimeout);
			httpGet.addHeader(headerconnection);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity responseEntity = httpResponse.getEntity();

			InputStream inputStream = responseEntity.getContent();
			InputStreamReader inputStremReader = new InputStreamReader(inputStream);
			BufferedReader in = new BufferedReader(inputStremReader);
			int responseStatusCode = httpResponse.getStatusLine().getStatusCode();
			if (responseStatusCode != 200) {
				CustomLogger.logInfo(String.format("The status is returned as %d for suite %s.", responseStatusCode,
						fitnesseSuite.getSuiteName()));
				throw new Exception("Did not get the right response from server.");
			}

			String inputLine;
			String oneResultNode = "";
			boolean collateNode = false;
			while ((inputLine = in.readLine()) != null) {
				if (collateNode) {
					oneResultNode += inputLine + "\n";
					if (inputLine.trim().startsWith("</result")) {
						processEachTestCaseFromOverviewResult(suiteId, oneResultNode);
						oneResultNode = "";
						collateNode = false;
					}
				}
				if (inputLine.trim().startsWith("<result")) {
					oneResultNode += inputLine + "\n";
					collateNode = true;
				}
			}
			in.close();
		} catch (IOException | AuthenticationException e) {
			CustomLogger.logError(e.toString());
		}
	}

	private void processEachTestCaseFromOverviewResult(int suiteId, String oneResultNode) {
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
			CustomLogger.logError(e.toString());
		}
	}

	public void updateResultDatabase(int suiteId, String testName, String status) {
		AllTestResult allTestCaseResult = testCaseResultDao.findTestCase(suiteId, testName);

		if (allTestCaseResult == null) {
			allTestCaseResult = new AllTestResult(suiteId, testName, status);
			allTestCaseResult.setLastExecutionTime(new Date(System.currentTimeMillis()));
			testCaseResultDao.createTestCaseResult(allTestCaseResult);
		} else if (allTestCaseResult.getStatus() == null || allTestCaseResult.getStatus().trim().length() == 0) {
			allTestCaseResult.setStatus(status);
			allTestCaseResult.setLastExecutionTime(new Date(System.currentTimeMillis()));
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

	public TestExecutionSettings getCurrentSettings() {
		return testExecutionSettingsDao.getCurrentSettings();
	}

	public int getPollingIntervalInMinutes() {
		return testExecutionSettingsDao.getPollingIntervalInMinutes();
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
