package com.bancoxyz.transformation.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.bancoxyz.transformation.model.entity.Interest;
import com.bancoxyz.transformation.model.input.InterestInput;

@Component
public class InterestSkipListener implements SkipListener<InterestInput, Interest> {

  private static final Logger logger = LoggerFactory.getLogger(InterestSkipListener.class);
  public static final List<InterestInput> skippedItems = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void onSkipInRead(@NonNull Throwable t) {
    logger.error("ERROR EN LECTURA - Error al leer línea del archivo: {}", t.getMessage());
    // En caso de error de lectura, creamos un InterestInput con valores por defecto para registrar el error
    InterestInput errorItem = new InterestInput();
    errorItem.setCuenta_id(0);
    errorItem.setNombre("ERROR_LECTURA");
    errorItem.setSaldo(null);
    errorItem.setEdad(0);
    errorItem.setTipo("ERROR");
    
    synchronized (skippedItems) {
      skippedItems.add(errorItem);
      logger.debug("Elemento de error de lectura agregado. Total elementos omitidos: {}", skippedItems.size());
    }
  }

   @Override
  public void onSkipInWrite(@NonNull Interest item, @NonNull Throwable t) {
    logger.error("Error al escribir: {}", t.getMessage());
  }

  @Override
  public void onSkipInProcess(@NonNull InterestInput item, @NonNull Throwable t) {
    logger.warn("SKIP DETECTED - Item omitido durante procesamiento: ID={}", item);
    
    synchronized (skippedItems) {
      skippedItems.add(item);
      logger.debug("Total elementos omitidos hasta ahora: {}", skippedItems.size());
    }
  }



}
