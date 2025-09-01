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


/**
 * Clase de configuración que define toda la configuración del job de Spring Batch
 * para el procesamiento de transacciones. Esta clase contiene todos los beans
 * necesarios para crear el pipeline de procesamiento: reader, processor, writer,
 * step, job y configuraciones adicionales como listeners y executors.
 * 
 * El flujo del job es:
 * 1. Leer transacciones desde CSV (TransactionItemReader)
 * 2. Procesar y validar cada transacción (TransactionItemProcessor)  
 * 3. Escribir transacciones válidas a base de datos (JpaItemWriter)
 * 4. Capturar transacciones inválidas y guardarlas en archivo de errores (listeners)
 */
@Configuration
public class TransactionBatchConfig {

  /**
   * Bean que define el procesador de elementos para aplicar lógica de negocio.
   * Este procesador valida y transforma objetos TransactionInput en entidades Transaction.
   * 
   * @return TransactionItemProcessor instancia del procesador
   */
  @Bean
  public TransactionItemProcessor transactionProcessor() {
    return new TransactionItemProcessor();
  }

  /**
   * Bean que define el writer JPA para guardar transacciones válidas en la base de datos.
   * Utiliza JPA/Hibernate para persistir las entidades Transaction en MySQL.
   * 
   * @param entityManagerFactory Factory de EntityManager para operaciones JPA
   * @return JpaItemWriter configurado para escribir entidades Transaction
   */
  @Bean
  public JpaItemWriter<Transaction> transactionWriter(EntityManagerFactory entityManagerFactory) {
    JpaItemWriter<Transaction> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(entityManagerFactory);
    return writer;
  }

  /**
   * Bean que define el writer para el archivo de errores (actualmente no utilizado directamente).
   * Este bean era para escribir directamente desde el SkipListener, pero ahora se crea
   * dinámicamente en JobCompletionTransactionListener para evitar problemas de ciclo de vida.
   * 
   * @return FlatFileItemWriter configurado para escribir errores en CSV
   */
  @Bean
  public FlatFileItemWriter<TransactionInput> transactionErrorWriter() {
    return new FlatFileItemWriterBuilder<TransactionInput>()
      .name("errorTransactionWriter")
      .resource(new FileSystemResource("errores-transacciones.csv"))
      .delimited()
      .names("id", "fecha", "monto", "tipo")
      .build();
  }

  /**
   * Bean que define el step principal del job de procesamiento de transacciones.
   * Configura el pipeline completo con:
   * - Chunk size de 10 elementos
   * - Reader, processor y writer
   * - Tolerancia a fallos con skip de InvalidDataException
   * - Límite de 100 skips
   * - Listener para capturar elementos omitidos
   * - Task executor para procesamiento paralelo con 3 threads
   * 
   * @param transactionReader Reader para leer desde CSV
   * @param jobRepository Repositorio de jobs de Spring Batch
   * @param itemWriter Writer JPA para base de datos
   * @param transactionManager Gestor de transacciones
   * @return Step configurado para el procesamiento
   */
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
      .skipLimit(1000)  // Aumentar límite para manejar más errores
      .listener(transactionSkipListener)
      .taskExecutor(transactionTaskExecutor())
      .build();
  }

  /**
   * Bean que configura el task executor para procesamiento paralelo.
   * Define un pool de threads para procesar múltiples transacciones en paralelo:
   * - Core pool: 3 threads
   * - Max pool: 3 threads  
   * - Queue capacity: 12 elementos
   * - Thread name prefix: "Nº"
   * 
   * @return ThreadPoolTaskExecutor configurado para el procesamiento paralelo
   */
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


  /**
   * Bean que define el job principal de procesamiento de transacciones.
   * Configura:
   * - Incrementer para generar parámetros únicos en cada ejecución
   * - Listener para manejar acciones al completar el job
   * - Step de procesamiento como único paso del job
   * 
   * @param jobRepository Repositorio de jobs de Spring Batch
   * @param stepTransactions Step de procesamiento definido anteriormente
   * @param listener Listener para acciones de finalización
   * @return Job completo configurado para procesar transacciones
   */
  @Bean
  public Job transactionJob(JobRepository jobRepository, Step stepTransactions, TransactionJobCompletionListener listener) {
    return new JobBuilder("transactionJob", jobRepository)
      .incrementer(new RunIdIncrementer())
      .listener(listener)
      .start(stepTransactions)
      .build();
  }


}
