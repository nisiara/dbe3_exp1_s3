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

/**
 * Procesador de elementos que implementa la lógica de negocio para transformar
 * objetos TransactionInput en entidades Transaction. Este componente es el núcleo
 * del procesamiento, donde se aplican las validaciones y transformaciones de datos.
 */
public class TransactionItemProcessor implements ItemProcessor<TransactionInput, Transaction> {
  private static final Logger logger = LoggerFactory.getLogger(TransactionItemProcessor.class);

  // Lista de formatos de fecha soportados
  private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
    DateTimeFormatter.ofPattern("yyyy/MM/dd")
  );

  /**
   * Procesa cada transacción individual aplicando validaciones de negocio y transformaciones.
   * Este método:
   * 1. Valida que el monto no sea null ni 0
   * 2. Valida y convierte la fecha de diferentes formatos a yyyy-MM-dd (LocalDate)
   * 3. Valida fechas inválidas como 2024-13-01
   * 4. Transforma TransactionInput en Transaction (entidad JPA)
   * 
   * Si alguna validación falla, lanza InvalidDataException que será capturada por Spring Batch,
   * marcando el registro como omitido (skip) y será guardado en el archivo de errores.
   * 
   * @param item Objeto TransactionInput leído desde el archivo CSV
   * @return Transaction entidad JPA lista para ser guardada en la base de datos
   * @throws Exception InvalidDataException si los datos no son válidos
   */
  @Override
  public Transaction process(@NonNull TransactionInput item) throws Exception {
    
    logger.debug("Realizando proceso en hilo: {} - Procesando item ID: {}, fecha: {}, monto: {}, tipo: {}", 
                Thread.currentThread().getName(), item.getId(), item.getFecha(), item.getMonto(), item.getTipo());
    
    // Validar monto no null y no cero
    if (item.getMonto() == null) {
      logger.warn("INVALID DATA - Transacción con monto null encontrada y será omitida: ID={}, fecha={}, tipo={}", 
                  item.getId(), item.getFecha(), item.getTipo());
      throw new InvalidDataException("El monto no puede ser null");
    }
    
    if (item.getMonto() == 0) {
      logger.warn("INVALID DATA - Transacción con monto 0 encontrada y será omitida: ID={}, fecha={}, tipo={}", 
                  item.getId(), item.getFecha(), item.getTipo());
      throw new InvalidDataException("El monto no puede ser 0");
    }
    
    // Validar y convertir fecha
    if (item.getFecha() == null || item.getFecha().trim().isEmpty()) {
      logger.warn("INVALID DATA - Transacción con fecha null o vacía encontrada y será omitida: ID={}, monto={}, tipo={}", 
                  item.getId(), item.getMonto(), item.getTipo());
      throw new InvalidDataException("La fecha no puede ser null o vacía");
    }
    
    LocalDate parsedDate = parseDate(item.getFecha());
    if (parsedDate == null) {
      logger.warn("INVALID DATA - Transacción con fecha inválida encontrada y será omitida: ID={}, fecha={}, monto={}, tipo={}", 
                  item.getId(), item.getFecha(), item.getMonto(), item.getTipo());
      throw new InvalidDataException("Fecha inválida: " + item.getFecha());
    }
    
    return new Transaction(
      item.getId(),
      parsedDate,
      item.getMonto(),
      item.getTipo()
    );
  }
  
  /**
   * Intenta parsear una fecha utilizando múltiples formatos soportados.
   * Si la fecha no puede ser parseada con ningún formato o contiene valores inválidos,
   * retorna null.
   * 
   * @param dateString Fecha en formato String
   * @return LocalDate parseada o null si no es válida
   */
  private LocalDate parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }
    
    String trimmedDate = dateString.trim();
    
    // Intentar parsear con cada formato soportado
    for (DateTimeFormatter formatter : DATE_FORMATTERS) {
      try {
        LocalDate date = LocalDate.parse(trimmedDate, formatter);
        logger.debug("Fecha parseada exitosamente: {} -> {}", dateString, date);
        return date;
      } catch (DateTimeParseException e) {
        // Continuar con el siguiente formato
        logger.debug("No se pudo parsear '{}' con formato {}", dateString, formatter);
      }
    }
    
    logger.warn("No se pudo parsear la fecha: {}", dateString);
    return null;
  }
}