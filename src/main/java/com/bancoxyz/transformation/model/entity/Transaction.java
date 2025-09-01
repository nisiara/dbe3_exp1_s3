package com.bancoxyz.transformation.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa una transacción en la base de datos.
 * Esta clase mapea a la tabla 'tbl_transactions' en MySQL y contiene
 * los datos procesados y validados de las transacciones.
 * 
 * Los campos han sido transformados desde TransactionInput:
 * - id: mantiene el mismo valor como clave primaria
 * - transactionDate: fecha convertida de String a LocalDate
 * - amount: monto validado (no puede ser 0)
 * - type: tipo de transacción (débito/crédito)
 * 
 * Utiliza Lombok para generar automáticamente getters, setters y constructores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "tbl_transactions")
public class Transaction {
  @Id
  private Long id;
  
  private LocalDate transactionDate;
  private Integer amount;
  private String type;

}