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
import com.bancoxyz.transformation.listener.InterestSkipListener;
import com.bancoxyz.transformation.listener.InterestJobCompletionListener;
import com.bancoxyz.transformation.model.entity.Interest;
import com.bancoxyz.transformation.model.input.InterestInput;
import com.bancoxyz.transformation.processor.InterestItemProcessor;
import com.bancoxyz.transformation.reader.InterestItemReader;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class InterestBatchConfig {

  @Bean
  public InterestItemProcessor interestProcessor() {
    return new InterestItemProcessor();
  }

  @Bean
  public JpaItemWriter<Interest> interestWriter(EntityManagerFactory entityManagerFactory){
    JpaItemWriter<Interest> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(entityManagerFactory);
    return writer;
  }

  @Bean
  public FlatFileItemWriter<Interest> interestErrorWriter() {
    return new FlatFileItemWriterBuilder<Interest>()
      .name("errorInterestWriter")
      .resource(new FileSystemResource("errores-intereses.csv"))
      .delimited()
      .names("cuenta_id", "nombre", "saldo", "edad", "tipo")
      .build();
  }

  @Bean
  public Step stepInterests(
    InterestItemReader interestReader,
    JobRepository jobRepository,
    JpaItemWriter<Interest> itemWriter,
    PlatformTransactionManager interestManager,
    InterestSkipListener interestSkipListener
  ) {
    return new StepBuilder("interestStep", jobRepository)
      .<InterestInput, Interest>chunk(10, interestManager)
      .reader(interestReader)
      .processor(interestProcessor())
      .writer(itemWriter)
      .faultTolerant()
      .skip(InvalidDataException.class)
      .skip(Exception.class) 
      .skipLimit(1000)
      .listener(interestSkipListener)
      .taskExecutor(interestTaskExecutor())
      .build();
  }

  @Bean
  public ThreadPoolTaskExecutor interestTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(30);
    executor.setThreadNamePrefix("Nº");
    executor.initialize();
    return executor;
  }

  @Bean
  public Job interestJob(JobRepository jobRepository, Step stepInterests, InterestJobCompletionListener listener) {
    return new JobBuilder("interestJob", jobRepository)
      .incrementer(new RunIdIncrementer())
      .listener(listener)
      .start(stepInterests)
      .build();
  }

}
