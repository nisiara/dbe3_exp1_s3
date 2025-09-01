package com.bancoxyz.transformation.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.bancoxyz.transformation.exception.InvalidDataException;
import com.bancoxyz.transformation.listener.AnnualAccountJobCompletionListener;
import com.bancoxyz.transformation.listener.AnnualAccountSkipListener;
import com.bancoxyz.transformation.model.entity.AnnualAccount;
import com.bancoxyz.transformation.model.input.AnnualAccountInput;
import com.bancoxyz.transformation.processor.AnnualAccountItemProcessor;
import com.bancoxyz.transformation.reader.AnnualAccountItemReader;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class AnnualAccountConfig {
  
  @Bean
  public AnnualAccountItemProcessor annualAccountProcessor() {
    return new AnnualAccountItemProcessor();
  }

  @Bean
  public JpaItemWriter<AnnualAccount> annualAccountWriter(EntityManagerFactory entityManagerFactory) {
    JpaItemWriter<AnnualAccount> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(entityManagerFactory);
    return writer;
  }

  @Bean
  public FlatFileItemWriter<AnnualAccountInput> annualAccountErrorWriter() {
    return new FlatFileItemWriterBuilder<AnnualAccountInput>()
      .name("errorAnnualAccountWriter")
      .resource(new FileSystemResource("errores-cuentas-anuales.csv"))
      .delimited()
      .names("cuenta_id", "fecha", "transaccion", "monto", "descripcion")
      .build();
  }

  @Bean
  public Step stepAnnualAccount(
    AnnualAccountItemReader annualAccountReader,
    JobRepository jobRepository,
    JpaItemWriter<AnnualAccount> itemWriter,
    PlatformTransactionManager annualAccountManager,
    AnnualAccountSkipListener annualAccountSkipListener
  ) {
    return new StepBuilder("annualAccountStep", jobRepository)
      .<AnnualAccountInput, AnnualAccount>chunk(10, annualAccountManager)
      .reader(annualAccountReader)
      .processor(annualAccountProcessor())
      .writer(itemWriter)
      .faultTolerant()
      .skip(InvalidDataException.class)
      .skipLimit(1000)
      .listener(annualAccountSkipListener)
      .taskExecutor(annualAccountTaskExecutor())
      .build();
  }

  @Bean
  public ThreadPoolTaskExecutor annualAccountTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(30);
    executor.setThreadNamePrefix("Nº");
    executor.initialize();
    return executor;
  }

  @Bean
  public Job annualAccountJob(JobRepository jobRepository, Step stepAnnualAccount, AnnualAccountJobCompletionListener listener) {
    return new JobBuilder("annualAccountJob", jobRepository)
      .incrementer(new RunIdIncrementer())
      .listener(listener)
      .start(stepAnnualAccount)
      .build();
  }


}
