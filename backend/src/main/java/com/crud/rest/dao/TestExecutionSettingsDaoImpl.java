package com.crud.rest.dao;

import java.util.Iterator;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.crud.rest.model.TestExecutionSettings;

@Repository
public class TestExecutionSettingsDaoImpl implements TestExecutionSettingsDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public int findIntervalBetweenExecutionTimeInSeconds() {
		Object valueToReturn = findValueFromSettings("executionInterval");
		//Return one day.
		if (valueToReturn == null)
			return 86400;
		return (int) valueToReturn;
	}

	private Object findValueFromSettings(String valuePassed) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			Criteria criteria = session.createCriteria(TestExecutionSettings.class);
			criteria.setProjection(Projections.property(valuePassed));
			Iterator<?> iterator = criteria.list().iterator();
			if (!iterator.hasNext())
				throw new Exception("Nothing was returned from database");
			int valueToReturn = (int) iterator.next();
			transaction.commit();
			session.close();
			return valueToReturn;
		} catch (Exception e) {
			session.close();
		}
		return null;
	}

	@Override
	public void setExecutionInterval(int interval) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {

			TestExecutionSettings settings = (TestExecutionSettings) session
					.createQuery("from com.crud.rest.dao.TestExecutionSettings").uniqueResult();

			settings.setExecutionInterval(interval);
			session.update(settings);
			transaction.commit();
			session.close();
		} catch (Exception e) {
			session.close();
		}
	}

	@Override
	public TestExecutionSettings getCurrentSettings() {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			Criteria criteria = session.createCriteria(TestExecutionSettings.class);
			Iterator<?> iterator = criteria.list().iterator();
			if (!iterator.hasNext())
				throw new Exception("Nothing was returned from database");
			TestExecutionSettings valueToReturn = (TestExecutionSettings) iterator.next();
			transaction.commit();
			session.close();
			return valueToReturn;
		} catch (Exception e) {
			session.close();
		}
		return null;
	}

	@Override
	public void setLastExecutionTime(String date) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {

			Query query = session.createQuery("from TestExecutionSettings");
			TestExecutionSettings settings = (TestExecutionSettings) query.uniqueResult();

			settings.setLastExecutionTime(date);
			session.update(settings);
			transaction.commit();
			session.close();
		} catch (Exception e) {
			session.close();
		}
	}

}
