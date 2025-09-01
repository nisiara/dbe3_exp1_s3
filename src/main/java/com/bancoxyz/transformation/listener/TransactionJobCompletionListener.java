package com.bancoxyz.transformation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.Chunk;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.NonNull;

import com.bancoxyz.transformation.model.input.TransactionInput;

import java.util.List;

/**
 * Listener que se ejecuta al completar un job de Spring Batch.
 * Su principal función es tomar todos los registros que fueron omitidos
 * durante el procesamiento (almacenados en TransactionSkipListener.skippedItems)
 * y escribirlos en un archivo CSV llamado "errores-transacciones.csv".
 * 
 * Este listener implementa el patrón de separación entre datos válidos e inválidos:
 * - Los datos válidos van a la base de datos (vía TransactionItemWriter)
 * - Los datos inválidos van al archivo CSV de errores (vía este listener)
 */
@Component
public class TransactionJobCompletionListener implements JobExecutionListener {

  private static final Logger logger = LoggerFactory.getLogger(TransactionJobCompletionListener.class);

  /**
   * Se ejecuta antes de iniciar el job.
   * Limpia la lista de registros omitidos del job anterior para evitar duplicaciones
   * y registra información sobre el inicio del job para propósitos de auditoría y monitoreo.
   * 
   * @param jobExecution Contexto de ejecución del job que contiene información sobre el job
   */
  @Override
  public void beforeJob(@NonNull JobExecution jobExecution) {
    logger.info("Iniciando el Job: {}", jobExecution.getJobInstance().getJobName());
    
    // Limpiar la lista de registros omitidos del job anterior para evitar duplicaciones
    TransactionSkipListener.skippedItems.clear();
    logger.info("Lista de registros omitidos limpiada para nueva ejecución");
    
    // Eliminar archivo de errores anterior si existe
    try {
      java.io.File errorFile = new java.io.File("errores-transacciones.csv");
      if (errorFile.exists()) {
        if (errorFile.delete()) {
          logger.info("Archivo de errores anterior eliminado exitosamente");
        } else {
          logger.warn("No se pudo eliminar el archivo de errores anterior");
        }
      }
    } catch (Exception e) {
      logger.warn("Error al intentar eliminar archivo de errores anterior: {}", e.getMessage());
    }
  }

  /**
   * Se ejecuta después de completar el job (exitoso o con errores).
   * Si el job fue exitoso, toma todos los registros omitidos acumulados en
   * TransactionSkipListener y los escribe en un archivo CSV llamado "errores-transacciones.csv".
   * 
   * Este método:
   * 1. Verifica el estado del job
   * 2. Si fue exitoso, obtiene los registros omitidos del TransactionSkipListener
   * 3. Crea un FlatFileItemWriter para el archivo de errores
   * 4. Escribe todos los registros omitidos con sus headers
   * 5. Cierra el writer y reporta la cantidad de registros guardados
   * 
   * @param jobExecution Contexto de ejecución del job que contiene el estado final
   */
  @Override
  public void afterJob(@NonNull JobExecution jobExecution) {
    logger.info("AFTER JOB - Job status: {}, Skipped items count: {}", 
                jobExecution.getStatus(), TransactionSkipListener.skippedItems.size());
                
    if (jobExecution.getStatus().isUnsuccessful()) {
      logger.error("Job finalizado con errores: {}", jobExecution.getJobInstance().getJobName());
    } else {
      logger.info("Job finalizado exitosamente: {}", jobExecution.getJobInstance().getJobName());
      
      // Escribir los registros omitidos en errores-transacciones.csv
      List<TransactionInput> skippedItems = TransactionSkipListener.skippedItems;
      logger.info("WRITING ERRORS - Iniciando escritura de {} registros omitidos al archivo CSV", skippedItems.size());

      if (!skippedItems.isEmpty()) {
        try {
          // Crear FlatFileItemWriter para archivo de errores
          FlatFileItemWriter<TransactionInput> errorWriter = new FlatFileItemWriter<>();
          errorWriter.setResource(new FileSystemResource("errores-transacciones.csv"));
          errorWriter.setHeaderCallback(writer -> writer.write("id,fecha,monto,tipo"));

          // Configurar line aggregator
          DelimitedLineAggregator<TransactionInput> lineAggregator = new DelimitedLineAggregator<>();
          lineAggregator.setDelimiter(",");

          BeanWrapperFieldExtractor<TransactionInput> fieldExtractor = new BeanWrapperFieldExtractor<>();
          fieldExtractor.setNames(new String[]{"id", "fecha", "monto", "tipo"});
          lineAggregator.setFieldExtractor(fieldExtractor);

          errorWriter.setLineAggregator(lineAggregator);

          // Abrir writer
          errorWriter.open(new ExecutionContext());

          // Escribir todos los registros omitidos (convertir List a Chunk)
          Chunk<TransactionInput> chunk = new Chunk<>(skippedItems);
          errorWriter.write(chunk);
          
          // Cerrar writer
          errorWriter.close();
          
          logger.info("WRITING ERRORS - Se escribieron {} registros omitidos en errores-transacciones.csv", skippedItems.size());
        } catch (Exception e) {
          logger.error("WRITING ERRORS - Error al escribir archivo de errores: {}", e.getMessage(), e);
        }
      } else {
        logger.info("WRITING ERRORS - No hay registros omitidos para escribir");
      }
    }
  }
}
