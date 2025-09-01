package com.bancoxyz.transformation.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class TransactionInput {
  private Long id;
  private String fecha;
  private Integer monto;
  private String tipo;

}
