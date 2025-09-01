package com.bancoxyz.transformation.exception;

/**
 * Excepción personalizada para manejar datos inválidos durante el procesamiento de transacciones.
 * Se lanza cuando una transacción no cumple con las reglas de validación del negocio,
 * como tener un monto igual a 0, lo que permite que Spring Batch maneje estos registros
 * como elementos omitidos (skip) en lugar de fallar todo el job.
 */
public class InvalidDataException extends Exception {
  
  /**
   * Constructor que crea una excepción con un mensaje descriptivo.
   * 
   * @param message Mensaje que describe la razón por la cual los datos son inválidos
   */
  public InvalidDataException(String message) {
    super(message);
  }

  /**
   * Constructor que crea una excepción con un mensaje y una causa subyacente.
   * 
   * @param message Mensaje que describe la razón por la cual los datos son inválidos
   * @param cause Excepción original que causó esta excepción
   */
  public InvalidDataException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
