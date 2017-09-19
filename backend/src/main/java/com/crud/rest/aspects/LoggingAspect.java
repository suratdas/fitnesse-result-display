package com.crud.rest.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.crud.rest.configuration.CustomLogger;
import com.crud.rest.model.FitnesseSuite;

@Aspect
@Component
public class LoggingAspect {

	@Pointcut("execution(* com.crud.rest.service.SuiteExecutionServiceImpl.executeSuite(..))")
	private void executeSuite() {
	}

	@Before("executeSuite()")
	public void beforeMethodExecution(JoinPoint theJoinPoint) {
		FitnesseSuite fitnesseSuite = (FitnesseSuite) theJoinPoint.getArgs()[0];
		CustomLogger.logInfo("Test execution for " + fitnesseSuite.getSuiteUrl() + " is started.");
	}

	@Around("executeSuite()")
	private Object afterMethodExecution(ProceedingJoinPoint theProceedingJoinPoint) throws Throwable {
		String methodName = theProceedingJoinPoint.getSignature().toShortString();
		Object[] args = theProceedingJoinPoint.getArgs();
		FitnesseSuite fitnesseSuite = (FitnesseSuite) args[0];
		Object result = theProceedingJoinPoint.proceed();
		CustomLogger.logInfo(String.format("%s method for %s is finished.", methodName, fitnesseSuite.getSuiteName()));
		return result;
	}

	
}
