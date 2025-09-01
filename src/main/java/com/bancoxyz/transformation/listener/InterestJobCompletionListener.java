package com.bancoxyz.transformation.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.bancoxyz.transformation.model.input.InterestInput;

@Component
public class InterestJobCompletionListener implements JobExecutionListener {
  
  private static final Logger logger = LoggerFactory.getLogger(InterestJobCompletionListener.class);

  @Override
  public void beforeJob(@NonNull JobExecution jobExecution) {
    logger.info("Iniciando el Job: {}", jobExecution.getJobInstance().getJobName());
    
    // Limpiar la lista de registros omitidos del job anterior para evitar duplicaciones
    InterestSkipListener.skippedItems.clear();
    logger.info("Lista de registros omitidos limpiada para nueva ejecución");
    
    // Eliminar archivo de errores anterior si existe
    try {
      java.io.File errorFile = new java.io.File("errores-intereses.csv");
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
    logger.info("AFTER JOB - Job status: {}, Skipped items count: {}", 
                jobExecution.getStatus(), InterestSkipListener.skippedItems.size());
                
    if (jobExecution.getStatus().isUnsuccessful()) {
      logger.error("Job finalizado con errores: {}", jobExecution.getJobInstance().getJobName());
    } else {
      logger.info("Job finalizado exitosamente: {}", jobExecution.getJobInstance().getJobName());
      
      // Escribir los registros omitidos en errores-transacciones.csv
      List<InterestInput> skippedItems = InterestSkipListener.skippedItems;
      logger.info("WRITING ERRORS - Iniciando escritura de {} registros omitidos al archivo CSV", skippedItems.size());

      if (!skippedItems.isEmpty()) {
        try {
          FlatFileItemWriter<InterestInput> errorWriter = new FlatFileItemWriter<>();
          errorWriter.setResource(new FileSystemResource("errores-intereses.csv"));
          errorWriter.setHeaderCallback(writer -> writer.write("cuenta_id,nombre,saldo,edad,tipo"));

          DelimitedLineAggregator<InterestInput> lineAggregator = new DelimitedLineAggregator<>();
          lineAggregator.setDelimiter(",");

          BeanWrapperFieldExtractor<InterestInput> fieldExtractor = new BeanWrapperFieldExtractor<>();
          fieldExtractor.setNames(new String[]{"cuenta_id", "nombre", "saldo", "edad", "tipo"});
          lineAggregator.setFieldExtractor(fieldExtractor);

          errorWriter.setLineAggregator(lineAggregator);

          // Abrir writer
          errorWriter.open(new ExecutionContext());

          // Escribir todos los registros omitidos (convertir List a Chunk)
          Chunk<InterestInput> chunk = new Chunk<>(skippedItems);
          errorWriter.write(chunk);
          
          // Cerrar writer
          errorWriter.close();
          
          logger.info("Se escribieron {} registros omitidos en errores-transacciones.csv", skippedItems.size());
        } catch (Exception e) {
          logger.error("Error al escribir archivo de errores: {}", e.getMessage(), e);
        }
      } else {
        logger.info("No hay registros omitidos para escribir");
      }
    }
  }



}
