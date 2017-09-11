package com.crud.rest.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.crud.rest.model.FitnesseSuite;

@Repository
public class FitnesseSuiteDaoImpl implements FitnesseSuiteDao {

	@Autowired
	private SessionFactory sessionFactory;

	/*
	 * public void setSessionFactory(SessionFactory sessionFactory) {
	 * this.sessionFactory = sessionFactory; }
	 */
	public FitnesseSuite findBySuiteId(String id) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		FitnesseSuite fitnesse = new FitnesseSuite();
		// It is not mandatory to specify fully qualified class name
		String queryString = "from com.crud.rest.model.FitnesseSuite where suiteId = ?";
		try {
			Query query = session.createQuery(queryString);
			query.setParameter(0, Integer.parseInt(id));
			fitnesse = (FitnesseSuite) query.uniqueResult();
			transaction.commit();
			session.close();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
		return fitnesse;
	}

	public FitnesseSuite findById(long id) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		FitnesseSuite fitnesse = new FitnesseSuite();
		try {
			fitnesse = (FitnesseSuite) session.get(FitnesseSuite.class, id);

			transaction.commit();
			session.close();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
		return fitnesse;
	}

	public FitnesseSuite findBySuiteName(String name) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		FitnesseSuite fitnesse = new FitnesseSuite();
		String hql = "from com.crud.rest.model.FitnesseSuite where suiteName = ?";
		try {
			Query query = session.createQuery(hql);
			query.setParameter(0, name);
			fitnesse = (FitnesseSuite) query.uniqueResult();
			transaction.commit();
			session.close();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
		return fitnesse;
	}

	public void saveSuite(FitnesseSuite suite) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		if (suite != null) {
			try {
				session.save(suite);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				session.close();
			}
		}
	}

	public void updateSuite(FitnesseSuite fitnesseResult) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		if (fitnesseResult != null) {
			try {
				session.update(fitnesseResult);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				session.close();
			}

		}

	}

	public void deleteSuiteById(int id) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		FitnesseSuite suite = new FitnesseSuite();
		try {
			suite = (FitnesseSuite) session.get(FitnesseSuite.class, id);
			session.delete(suite);
			transaction.commit();
			session.close();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}

	}

	@SuppressWarnings("unchecked")
	public List<FitnesseSuite> findAllSuites() {
		List<FitnesseSuite> suites = new ArrayList<FitnesseSuite>();
		Session session = sessionFactory.openSession();
		// Session session = sessionFactory.getCurrentSession();

		// Example of calling a stored procedure
		/*
		 * Query query = session.createSQLQuery( "CALL getAllSuites()")
		 * .addEntity(FitnesseSuite.class); suites = query.list();
		 */

		// Example of calling using hibernate query
		suites = session.createQuery("from FitnesseSuite").list();

		return suites;
	}

	public void deleteAllSuites() {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.createQuery("delete from FitnesseSuite").executeUpdate();
			transaction.commit();
			session.close();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
	}

	public boolean isSuiteExist(FitnesseSuite result) {
		return findBySuiteName(result.getSuiteName()) != null;
	}

	@Override
	public void updateTestSuite(FitnesseSuite fitnesseSuite) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.update(fitnesseSuite);
			transaction.commit();
			session.close();
		} catch (Exception e) {
			transaction.rollback();
			session.close();
		}
	}

}
