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

  /**
   * Se ejecuta cuando ocurre un skip durante la fase de lectura.
   * Esto puede suceder si hay problemas al leer una línea del archivo CSV.
   * 
   * @param t Excepción que causó el skip en la lectura
   */
  @Override
  public void onSkipInRead(@NonNull Throwable t) {
    logger.error("Error al leer: {}", t.getMessage());
  }

  /**
   * Se ejecuta cuando ocurre un skip durante la fase de escritura.
   * Esto puede suceder si hay problemas al escribir en la base de datos.
   * 
   * @param item Elemento que se intentaba escribir cuando ocurrió el error
   * @param t Excepción que causó el skip en la escritura
   */
  @Override
  public void onSkipInWrite(@NonNull Transaction item, @NonNull Throwable t) {
    logger.error("Error al escribir: {}", t.getMessage());
  }

  /**
   * Se ejecuta cuando ocurre un skip durante la fase de procesamiento.
   * Este es el método más importante ya que captura los registros que fallan
   * la validación de negocio (como montos = 0) y los almacena para ser
   * posteriormente escritos en el archivo de errores.
   * 
   * @param item TransactionInput que causó el skip durante el procesamiento
   * @param t Excepción que causó el skip (típicamente InvalidDataException)
   */
  @Override
  public void onSkipInProcess(@NonNull TransactionInput item, @NonNull Throwable t) {
    logger.warn("SKIP DETECTED - Item omitido durante procesamiento: ID={}, fecha={}, monto={}, tipo={}, Error: {}", 
                item.getId(), item.getFecha(), item.getMonto(), item.getTipo(), t.getMessage());
    
    synchronized (skippedItems) {
      skippedItems.add(item);
      logger.debug("Total elementos omitidos hasta ahora: {}", skippedItems.size());
    }
  }
}