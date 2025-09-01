package com.bancoxyz.transformation.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import com.bancoxyz.transformation.exception.InvalidDataException;
import com.bancoxyz.transformation.model.entity.AnnualAccount;
import com.bancoxyz.transformation.model.input.AnnualAccountInput;

public class AnnualAccountItemProcessor implements ItemProcessor<AnnualAccountInput, AnnualAccount>{
  
  private static final Logger logger = LoggerFactory.getLogger(AnnualAccountItemProcessor.class);

  private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
    DateTimeFormatter.ofPattern("yyyy/MM/dd")
  );

  @Override
  public AnnualAccount process(@NonNull AnnualAccountInput item) throws Exception {
    
    logger.debug("Realizando proceso en hilo: {} - Procesando item ID: {}, ", item);
    
    if (item.getMonto() == null) {
      logger.warn("DATAO INVALIDO - Transacción con monto null encontrada y será omitida: ", item);
      throw new InvalidDataException("El monto no puede ser null");
    }
    
    if (item.getFecha() == null || item.getFecha().trim().isEmpty()) {
      logger.warn("DATO INVALIDO - Transacción con fecha null o vacía encontrada y será omitida: ", item);
      throw new InvalidDataException("La fecha no puede ser null o vacía");
    }
    
    LocalDate parsedDate = parseDate(item.getFecha());
    if (parsedDate == null) {
      logger.warn("INVALID DATA - Transacción con fecha inválida encontrada y será omitida: ", item);
      throw new InvalidDataException("Fecha inválida: " + item.getFecha());
    }
    
    return new AnnualAccount(
      null,
      item.getCuenta_id(),
      parsedDate,
      item.getTransaccion(),
      item.getMonto(),
      item.getDescripcion()
    );
  }

  private LocalDate parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }
    
    String trimmedDate = dateString.trim();
    
    
    for (DateTimeFormatter formatter : DATE_FORMATTERS) {
      try {
        LocalDate date = LocalDate.parse(trimmedDate, formatter);
        logger.debug("Fecha parseada exitosamente: {} -> {}", dateString, date);
        return date;
      } catch (DateTimeParseException e) {
        
        logger.debug("No se pudo parsear '{}' con formato {}", dateString, formatter);
      }
    }
    
    logger.warn("No se pudo parsear la fecha: {}", dateString);
    return null;
  }


}
