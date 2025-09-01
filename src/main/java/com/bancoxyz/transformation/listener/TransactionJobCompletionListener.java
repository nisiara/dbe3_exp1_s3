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
    
    TransactionSkipListener.skippedItems.clear();
    logger.info("Lista de registros omitidos limpiada para nueva ejecución");
    
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

  @Override
  public void afterJob(@NonNull JobExecution jobExecution) {
    logger.info("Job status: {}, items omitidos: {}", 
                jobExecution.getStatus(), TransactionSkipListener.skippedItems.size());
                
    if (jobExecution.getStatus().isUnsuccessful()) {
      logger.error("Job finalizado con errores: {}", jobExecution.getJobInstance().getJobName());
    } else {
      logger.info("Job finalizado exitosamente: {}", jobExecution.getJobInstance().getJobName());
      
      List<TransactionInput> skippedItems = TransactionSkipListener.skippedItems;
      logger.info("Iniciando escritura de {} registros omitidos al archivo CSV", skippedItems.size());

      if (!skippedItems.isEmpty()) {
        try {
          FlatFileItemWriter<TransactionInput> errorWriter = new FlatFileItemWriter<>();
          errorWriter.setResource(new FileSystemResource("errores-transacciones.csv"));
          errorWriter.setHeaderCallback(writer -> writer.write("id,fecha,monto,tipo"));

          DelimitedLineAggregator<TransactionInput> lineAggregator = new DelimitedLineAggregator<>();
          lineAggregator.setDelimiter(",");

          BeanWrapperFieldExtractor<TransactionInput> fieldExtractor = new BeanWrapperFieldExtractor<>();
          fieldExtractor.setNames(new String[]{"id", "fecha", "monto", "tipo"});
          lineAggregator.setFieldExtractor(fieldExtractor);

          errorWriter.setLineAggregator(lineAggregator);

          errorWriter.open(new ExecutionContext());

          Chunk<TransactionInput> chunk = new Chunk<>(skippedItems);
          errorWriter.write(chunk);
          
          errorWriter.close();
          
          logger.info("Se escribieron {} registros omitidos en errores-transacciones.csv", skippedItems.size());
        } catch (Exception e) {
          logger.error("Error al escribir archivo de errores: {}", e.getMessage(), e);
        }
      } else {
        logger.info("No hay datos inválidos para registrar");
      }
    }
  }
}
