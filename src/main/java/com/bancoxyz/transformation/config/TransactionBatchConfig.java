package com.bancoxyz.transformation.config;

import com.bancoxyz.transformation.exception.InvalidDataException;
import com.bancoxyz.transformation.listener.TransactionJobCompletionListener;
import com.bancoxyz.transformation.listener.TransactionSkipListener;
import com.bancoxyz.transformation.model.entity.Transaction;
import com.bancoxyz.transformation.model.input.TransactionInput;
import com.bancoxyz.transformation.processor.TransactionItemProcessor;
import com.bancoxyz.transformation.reader.TransactionItemReader;

import jakarta.persistence.EntityManagerFactory;
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

@Configuration
public class TransactionBatchConfig {

  @Bean
  public TransactionItemProcessor transactionProcessor() {
    return new TransactionItemProcessor();
  }

  @Bean
  public JpaItemWriter<Transaction> transactionWriter(EntityManagerFactory entityManagerFactory) {
    JpaItemWriter<Transaction> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(entityManagerFactory);
    return writer;
  }

  @Bean
  public FlatFileItemWriter<TransactionInput> transactionErrorWriter() {
    return new FlatFileItemWriterBuilder<TransactionInput>()
      .name("errorTransactionWriter")
      .resource(new FileSystemResource("errores-transacciones.csv"))
      .delimited()
      .names("id", "fecha", "monto", "tipo")
      .build();
  }

  @Bean
  public Step stepTransactions(
    TransactionItemReader transactionReader,
    JobRepository jobRepository,
    JpaItemWriter<Transaction> itemWriter,
    PlatformTransactionManager transactionManager,
    TransactionSkipListener transactionSkipListener
  ) {
    return new StepBuilder("transactionStep", jobRepository)
      .<TransactionInput, Transaction>chunk(10, transactionManager)
      .reader(transactionReader)
      .processor(transactionProcessor())
      .writer(itemWriter)
      .faultTolerant()
      .skip(InvalidDataException.class)
      .skipLimit(1000)
      .listener(transactionSkipListener)
      .taskExecutor(transactionTaskExecutor())
      .build();
  }

  @Bean
  public ThreadPoolTaskExecutor transactionTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(30);
    executor.setThreadNamePrefix("Nº");
    executor.initialize();
    return executor;
  }


  @Bean
  public Job transactionJob(JobRepository jobRepository, Step stepTransactions, TransactionJobCompletionListener listener) {
    return new JobBuilder("transactionJob", jobRepository)
      .incrementer(new RunIdIncrementer())
      .listener(listener)
      .start(stepTransactions)
      .build();
  }


}
