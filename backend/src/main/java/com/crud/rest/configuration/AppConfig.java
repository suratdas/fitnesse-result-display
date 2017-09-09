package com.crud.rest.configuration;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
@EnableWebMvc
@ComponentScan(basePackages = "com.crud.rest.")
@EnableScheduling
@PropertySource(value = { "classpath:properties/${property:defaultValue}.properties" }, ignoreResourceNotFound = true)
public class AppConfig implements SchedulingConfigurer {
	// @Bean configurations go here...

	@Value("${db.url}")
	private String dbUrl;

	@Value("${db.driver}")
	private String dbDriver;

	@Value("${db.username}")
	private String dbUsername;

	@Value("${db.password}")
	private String dbPassword;

	@Bean
	public static PropertyPlaceholderConfigurer properties() {

		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
		ClassPathResource[] resources = new ClassPathResource[] { new ClassPathResource("db.properties") };
		// FileSystemResource resources = new FileSystemResource(new File("db.properties"));
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
		dataSource.setPassword(dbPassword);

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
		properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		properties.put("hibernate.hbm2ddl.auto", "update");

		return properties;
	}

	@Bean(name = "transactionManager")
	public HibernateTransactionManager getTransactionManager(SessionFactory sessionFactory) {
		HibernateTransactionManager transactionManager = new HibernateTransactionManager(sessionFactory);

		return transactionManager;
	}

	@Bean
	public ScheduledTasks myBean() {
		return new ScheduledTasks();
	}

	@Bean(destroyMethod = "shutdown")
	public Executor taskExecutor() {
		return Executors.newScheduledThreadPool(100);
	}

	@Autowired
	private SuiteExecutionServiceImpl testExecutionService;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskExecutor());
		taskRegistrar.addTriggerTask(new Runnable() {
			@Override
			public void run() {
				TestExecutionSettings testExecutionSettings = testExecutionService.findCurrentSettings();
				myBean().triggerTestExecution(testExecutionSettings.getFitnesseUserName(),
						testExecutionSettings.getFitnessePassword());
			}
		}, new Trigger() {
			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {
				Calendar nextExecutionTime = new GregorianCalendar();
				Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
				nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
				int interval = testExecutionService.findIntervalBetweenExecutionTimeInSeconds();
				// nextExecutionTime.add(Calendar.MILLISECOND, env.getProperty("myRate", Integer.class));
				nextExecutionTime.add(Calendar.SECOND, interval);
				return nextExecutionTime.getTime();
			}
		});
	}

}
