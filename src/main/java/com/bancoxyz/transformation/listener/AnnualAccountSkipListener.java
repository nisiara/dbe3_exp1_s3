package com.bancoxyz.transformation.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.bancoxyz.transformation.model.entity.AnnualAccount;
import com.bancoxyz.transformation.model.input.AnnualAccountInput;

@Component
public class AnnualAccountSkipListener implements SkipListener<AnnualAccountInput, AnnualAccount>{
 
  private static final Logger logger = LoggerFactory.getLogger(TransactionSkipListener.class);
  public static final List<AnnualAccountInput> skippedItems = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void onSkipInRead(@NonNull Throwable t) {
    logger.error("Error al leer: {}", t.getMessage());
  }

  @Override
  public void onSkipInWrite(@NonNull AnnualAccount item, @NonNull Throwable t) {
    logger.error("Error al escribir: {}", t.getMessage());
  }

  @Override
  public void onSkipInProcess(@NonNull AnnualAccountInput item, @NonNull Throwable t) {
    logger.warn("Item omitido durante procesamiento: ", t.getMessage());

    synchronized (skippedItems) {
      skippedItems.add(item);
      logger.debug("Total elementos omitidos hasta ahora: {}", skippedItems.size());
    }
  }

}
