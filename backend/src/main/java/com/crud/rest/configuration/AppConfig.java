package com.crud.rest.configuration;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.crud.rest.model.AllTestResult;
import com.crud.rest.model.FitnesseSuite;
import com.crud.rest.model.TestExecutionSettings;
import com.crud.rest.service.SuiteExecutionServiceImpl;

@Configuration
@ComponentScan(basePackages = "com.crud.rest.")
@EnableAspectJAutoProxy
@EnableWebMvc
@EnableScheduling
@PropertySource(value = { "classpath:properties/${property:defaultValue}.properties" }, ignoreResourceNotFound = true)
public class AppConfig implements SchedulingConfigurer {

	public static final String encryptionSeed = "aFnT*^$8";

	@Value("${db.url}")
	private String dbUrl;

	@Value("${db.driver}")
	private String dbDriver;

	@Value("${db.username}")
	private String dbUsername;

	@Value("${db.password}")
	private String dbPassword;

	@Value("${hibernate.dialect}")
	private String hibernateDialect;

	@Value("${logFilePath}")
	private String logFilePath;

	@Value("${resultPublishServerAddress}")
	private String resultPublishServerAddress;

	@Bean
	public static PropertyPlaceholderConfigurer properties() {

		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
		ClassPathResource[] resources = new ClassPathResource[] { new ClassPathResource("db.properties") };
		ppc.setLocations(resources);
		ppc.setIgnoreUnresolvablePlaceholders(true);
		ppc.setSearchSystemEnvironment(true);
		return ppc;
	}

	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(dbUrl);
		dataSource.setDriverClassName(dbDriver);
		dataSource.setUsername(dbUsername);

		//Use Jasypt API with same logic as below to encrypt and put the encrypted password in the db.properties file.
		StandardPBEStringEncryptor encryptDecryptor = new StandardPBEStringEncryptor();
		encryptDecryptor.setPassword(encryptionSeed);
		String decryptedDatabasePassword = encryptDecryptor.decrypt(dbPassword);

		dataSource.setPassword(decryptedDatabasePassword);

		return dataSource;
	}

	@Bean(name = "sessionFactory")
	public SessionFactory getSessionFactory(DataSource dataSource) {

		LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(dataSource);

		sessionBuilder.addAnnotatedClasses(FitnesseSuite.class, AllTestResult.class, TestExecutionSettings.class);
		sessionBuilder.addProperties(getHibernateProperties());

		return sessionBuilder.buildSessionFactory();
	}

	private Properties getHibernateProperties() {
		Properties properties = new Properties();
		properties.put("hibernate.show_sql", "true");
		properties.put("hibernate.dialect", hibernateDialect);
		properties.put("hibernate.hbm2ddl.auto", "update");

		return properties;
	}

	@Bean(name = "transactionManager")
	public HibernateTransactionManager getTransactionManager(SessionFactory sessionFactory) {
		HibernateTransactionManager transactionManager = new HibernateTransactionManager(sessionFactory);

		return transactionManager;
	}

	@Bean(name = "scheduledTasks")
	public ScheduledTasks myBean() {
		return new ScheduledTasks(resultPublishServerAddress);
	}

	@Bean(destroyMethod = "shutdown")
	public Executor taskExecutor() {
		return Executors.newScheduledThreadPool(100);
	}

	@Autowired
	private SuiteExecutionServiceImpl testExecutionService;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		CustomLogger.setLogFilePath(logFilePath);
		taskRegistrar.setScheduler(taskExecutor());
		taskRegistrar.addTriggerTask(new TriggerredTask(), new SchedulingTrigger());
	}

	private class SchedulingTrigger implements Trigger {
		@Override
		public Date nextExecutionTime(TriggerContext triggerContext) {
			Calendar nextExecutionTime = new GregorianCalendar();
			Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
			nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
			int interval = testExecutionService.getPollingIntervalInMinutes();
			//Prevent side effect of accidental setting of undesired value in database
			if (interval < 1)
				interval = 1;
			nextExecutionTime.add(Calendar.MINUTE, interval);
			return nextExecutionTime.getTime();
		}
	}

	private class TriggerredTask implements Runnable {
		@Override
		public void run() {

			TestExecutionSettings testExecutionSettings = testExecutionService.getCurrentSettings();
			Date nextExecutionTime = testExecutionSettings.getNextExecutionTime();

			try {
				if (testExecutionSettings.isRunning())
					return;
				CustomLogger.logInfo("Polling at " + new Date());
				if (nextExecutionTime == null || nextExecutionTime.before(new Date())) {
					myBean().triggerTestExecution(testExecutionSettings.getFitnesseUserName(),
							testExecutionSettings.getFitnessePassword(), false);
					return;
				}
			} catch (Exception e) {
				CustomLogger.logError(e.toString() + "\n" + e.getMessage());
			}
		}
	}

}
