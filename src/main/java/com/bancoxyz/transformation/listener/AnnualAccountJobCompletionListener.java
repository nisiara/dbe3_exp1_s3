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

import com.bancoxyz.transformation.model.input.AnnualAccountInput;

@Component
public class AnnualAccountJobCompletionListener implements JobExecutionListener {

  private static final Logger logger = LoggerFactory.getLogger(AnnualAccountJobCompletionListener.class);

  @Override
  public void beforeJob(@NonNull JobExecution jobExecution) {
    logger.info("Iniciando el Job: {}", jobExecution.getJobInstance().getJobName());
    
    AnnualAccountSkipListener.skippedItems.clear();
    logger.info("Lista de registros omitidos limpiada para nueva ejecución");
    
    try {
      java.io.File errorFile = new java.io.File("errores-cuentas-anuales.csv");
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
    logger.info("Job status: {}, items omitidos: {}", jobExecution.getStatus(), AnnualAccountSkipListener.skippedItems.size());
                
    if (jobExecution.getStatus().isUnsuccessful()) {
      logger.error("Job finalizado con errores: {}", jobExecution.getJobInstance().getJobName());
    } else {
      logger.info("Job finalizado exitosamente: {}", jobExecution.getJobInstance().getJobName());
      
      List<AnnualAccountInput> skippedItems = AnnualAccountSkipListener.skippedItems;
      logger.info("Iniciando escritura de {} registros considerados inválidos al archivo CSV", skippedItems.size());

      if (!skippedItems.isEmpty()) {
        try {
          FlatFileItemWriter<AnnualAccountInput> errorWriter = new FlatFileItemWriter<>();
          errorWriter.setResource(new FileSystemResource("errores-cuentas-anuales.csv"));
          errorWriter.setHeaderCallback(writer -> writer.write("cuenta_id,fecha,transaccion,monto,descripcion"));

          DelimitedLineAggregator<AnnualAccountInput> lineAggregator = new DelimitedLineAggregator<>();
          lineAggregator.setDelimiter(",");

          BeanWrapperFieldExtractor<AnnualAccountInput> fieldExtractor = new BeanWrapperFieldExtractor<>();
          fieldExtractor.setNames(new String[]{"cuenta_id", "fecha", "transaccion", "monto", "descripcion"});
          lineAggregator.setFieldExtractor(fieldExtractor);

          errorWriter.setLineAggregator(lineAggregator);

          errorWriter.open(new ExecutionContext());

          Chunk<AnnualAccountInput> chunk = new Chunk<>(skippedItems);
          errorWriter.write(chunk);
          
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
