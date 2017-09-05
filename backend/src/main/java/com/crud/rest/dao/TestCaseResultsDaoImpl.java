package com.crud.rest.dao;

import java.util.Iterator;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.crud.rest.model.AllTestResult;

@Repository
public class TestCaseResultsDaoImpl implements TestCaseResultDao {

	public enum DeleteType {
		All, UnusedTestCases, AllForThisSuite
	}

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void updateTestCaseResult(AllTestResult fitnesseTestCaseResult) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.update(fitnesseTestCaseResult);
			transaction.commit();
			session.close();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
	}

	@Override
	public AllTestResult findTestCase(int suiteId, String testName) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			Criteria criteria = session.createCriteria(AllTestResult.class)
					.add(Restrictions.and(Restrictions.eq("suiteId", suiteId), Restrictions.eq("testName", testName)));
			Iterator<?> iterator = criteria.list().iterator();
			if (!iterator.hasNext())
				return null;
			AllTestResult itemToBeReturned = (AllTestResult) iterator.next();

			// Delete any duplicate test result for the test name.
			while (iterator.hasNext())
				session.delete(iterator.next());

			transaction.commit();
			session.close();
			return itemToBeReturned;
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
		return null;
	}

	@Override
	public void createTestCaseResult(AllTestResult fitnesseTestCaseResult) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.save(fitnesseTestCaseResult);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
	}

	@Override
	public void clearAllTestResult(int suiteId) {
		Session session = sessionFactory.openSession();
		try {
			String queryString = "update AllTestResult a set a.status='' where suiteId='" + suiteId + "'";
			Query query = session.createQuery(queryString);
			query.executeUpdate();
			session.close();
		} catch (Exception e) {
			session.close();
		}
	}

	@Override
	public void deleteTestCaseResults(int suiteId, DeleteType deleteType) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {

			Criteria criteria = session.createCriteria(AllTestResult.class);
			Criterion suiteCriterion = Restrictions.eq("suiteId", suiteId);
			Criterion unusedCriterion = Restrictions.eq("status", "");
			if (deleteType == DeleteType.UnusedTestCases)
				criteria.add(Restrictions.and(suiteCriterion, unusedCriterion));
			else if (deleteType == DeleteType.AllForThisSuite)
				criteria.add(suiteCriterion);
			else if (deleteType == DeleteType.All) {
				session.createQuery("delete from AllTestResult").executeUpdate();
				transaction.commit();
				session.close();
				return;
			}
			Iterator<?> iterator = criteria.list().iterator();
			if (!iterator.hasNext())
				return;

			// Delete any unused test result
			while (iterator.hasNext())
				session.delete(iterator.next());

			transaction.commit();
			session.close();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
	}

	@Override
	public int findTestCases(int suiteId, String status) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			Criteria criteria = session.createCriteria(AllTestResult.class)
					.add(Restrictions.and(Restrictions.eq("suiteId", suiteId), Restrictions.eq("status", status)));
			transaction.commit();
			int valueToReturn = criteria.list().size();
			session.close();
			return valueToReturn;
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
		return 0;
	}

}
