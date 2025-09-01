package com.bancoxyz.transformation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

import com.bancoxyz.transformation.model.entity.Transaction;
import com.bancoxyz.transformation.model.input.TransactionInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Listener que captura los elementos que son omitidos (skipped) durante el procesamiento batch.
 * Este listener implementa SkipListener para manejar los diferentes tipos de skip que pueden
 * ocurrir en las fases de lectura, procesamiento y escritura del job de Spring Batch.
 * 
 * Los registros omitidos se almacenan en memoria para ser posteriormente escritos
 * en un archivo de errores al finalizar el job.
 */
@Component
public class TransactionSkipListener implements SkipListener<TransactionInput, Transaction> {

  private static final Logger logger = LoggerFactory.getLogger(TransactionSkipListener.class);
  public static final List<TransactionInput> skippedItems = Collections.synchronizedList(new ArrayList<>());

  
  @Override
  public void onSkipInRead(@NonNull Throwable t) {
    logger.error("Error al leer: {}", t.getMessage());
  }

  @Override
  public void onSkipInWrite(@NonNull Transaction item, @NonNull Throwable t) {
    logger.error("Error al escribir: {}", t.getMessage());
  }

  @Override
  public void onSkipInProcess(@NonNull TransactionInput item, @NonNull Throwable t) {
    logger.warn("Item omitido durante procesamiento: {}", item, t.getMessage());
    
    synchronized (skippedItems) {
      skippedItems.add(item);
      logger.debug("Total elementos omitidos hasta ahora: {}", skippedItems.size());
    }
  }
}