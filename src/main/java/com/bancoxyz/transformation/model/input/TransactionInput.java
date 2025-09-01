package com.bancoxyz.transformation.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase de modelo que representa la estructura de datos de entrada leídos desde el archivo CSV.
 * Esta clase mapea directamente a los campos del archivo de transacciones.csv:
 * - id: identificador único de la transacción
 * - fecha: fecha de la transacción en formato String (será convertida a LocalDate en el procesamiento)
 * - monto: cantidad monetaria de la transacción (puede ser positiva o negativa)
 * - tipo: tipo de transacción (débito o crédito)
 * 
 * Utiliza Lombok para generar automáticamente getters, setters, constructores y métodos utilitarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class TransactionInput {
  private Long id;
  private String fecha; // Vuelve a ser String para manejar diferentes formatos
  private Integer monto;
  private String tipo;

}
