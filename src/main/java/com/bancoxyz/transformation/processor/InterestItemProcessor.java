package com.bancoxyz.transformation.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import com.bancoxyz.transformation.exception.InvalidDataException;
import com.bancoxyz.transformation.model.entity.Interest;
import com.bancoxyz.transformation.model.input.InterestInput;

public class InterestItemProcessor implements ItemProcessor<InterestInput, Interest>{

  private static final Logger logger = LoggerFactory.getLogger(InterestItemProcessor.class);
  
  @Override
  public Interest process(@NonNull InterestInput item) throws Exception {
    
    if(item.getNombre() == null || item.getNombre().equalsIgnoreCase("Unknown") || item.getNombre().trim().isEmpty()) {
      logger.warn("DATO INVALIDO - Item con nombre de cliente desconocido: {}", item);
      throw new InvalidDataException("Debe existir el nombre de cliente");
    }

    if(item.getSaldo() == null) {
      logger.warn("DATO INVALIDO - Item con saldo nulo: {}", item);
      throw new InvalidDataException("Debe existir el saldo");
    }

    if(item.getTipo() == null || item.getTipo().equals("-1") || item.getTipo().trim().isEmpty()) {
      logger.warn("DATO INVALIDO - Item con tipo inválido: {}", item);
      throw new InvalidDataException("Debe existir el tipo");
    }

    if(item.getCuenta_id() <= 0) {
      logger.warn("DATO INVALIDO - Item con cuenta_id inválido: {}", item);
      throw new InvalidDataException("Debe existir un cuenta_id válido");
    }

    if(item.getEdad() <= 0) {
      logger.warn("DATO INVALIDO - Item con edad inválida: {}", item);
      throw new InvalidDataException("Debe existir una edad válida");
    }

    return new Interest(
      null,
      item.getCuenta_id(),
      item.getNombre(),
      item.getSaldo(),
      item.getEdad(),
      item.getTipo()
    );

  }
  
}
