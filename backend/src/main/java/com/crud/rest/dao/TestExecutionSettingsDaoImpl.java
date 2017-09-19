package com.crud.rest.dao;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.Criteria;
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
	public int getPollingIntervalInMinutes() {
		Object valueToReturn = findValueFromSettings("pollingInterval");
		//Return one day.
		if (valueToReturn == null)
			return 1440;
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
			e.printStackTrace();
			session.close();
		}
		return null;
	}

	@Override
	public void setPollingInterval(int interval) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {

			TestExecutionSettings settings = (TestExecutionSettings) session
					.createQuery("from TestExecutionSettings").uniqueResult();

			settings.setPollingInterval(interval);
			session.update(settings);
			transaction.commit();
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
			session.close();
		}
		return null;
	}

	@Override
	public void setNextExecutionTime(Date date) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {

			TestExecutionSettings settings = (TestExecutionSettings) session
					.createQuery("from TestExecutionSettings").uniqueResult();

			settings.setNextExecutionTime(date);
			session.update(settings);
			transaction.commit();
			session.close();
		} catch (Exception e) {
			session.close();
		}
	}
	
	@Override
	public void updateTestExecutionSettings(TestExecutionSettings settings){
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.update(settings);
			transaction.commit();
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
			session.close();
		}
		
	}

}
