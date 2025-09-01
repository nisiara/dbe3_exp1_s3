package com.bancoxyz.transformation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.lang.NonNull;

public class AnnualAccountJobListener implements JobExecutionListener{

  private static final Logger logger = LoggerFactory.getLogger(AnnualAccountJobListener.class);

  @Override
  public void beforeJob(@NonNull JobExecution jobExecution) {
    logger.info("Inicio del Job: {}", jobExecution.getJobInstance().getJobName());
  }

  @Override
  public void afterJob(@NonNull JobExecution jobExecution) {
    logger.info("Fin del Job: {}", jobExecution.getJobInstance().getJobName());
  }
}
