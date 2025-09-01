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
import com.bancoxyz.transformation.model.entity.Transaction;
import com.bancoxyz.transformation.model.input.TransactionInput;


public class TransactionItemProcessor implements ItemProcessor<TransactionInput, Transaction> {
  
  private static final Logger logger = LoggerFactory.getLogger(TransactionItemProcessor.class);

  private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
    DateTimeFormatter.ofPattern("yyyy/MM/dd")
  );

  @Override
  public Transaction process(@NonNull TransactionInput item) throws Exception {
    
    logger.debug("Realizando proceso en hilo: {} - Procesando item ID: {}", Thread.currentThread().getName());
    
    if (item.getMonto() == null) {
      logger.warn("Transacción con monto null encontrada y será omitida: {}", item);
      throw new InvalidDataException("El monto no puede ser null");
    }
    
    if (item.getMonto() == 0) {
      logger.warn("Transacción con monto 0 encontrada y será omitida: {}", item);
      throw new InvalidDataException("El monto no puede ser 0");
    }
    
    // Validar y convertir fecha
    if (item.getFecha() == null || item.getFecha().trim().isEmpty()) {
      logger.warn("Transacción con fecha null o vacía encontrada y será omitida: {}", item);
      throw new InvalidDataException("La fecha no puede ser null o vacía");
    }
    
    LocalDate parsedDate = parseDate(item.getFecha());
    if (parsedDate == null) {
      logger.warn("Transacción con fecha inválida encontrada y será omitida: {}", item);
      throw new InvalidDataException("Fecha inválida: " + item.getFecha());
    }
    
    return new Transaction(
      item.getId(),
      parsedDate,
      item.getMonto(),
      item.getTipo()
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