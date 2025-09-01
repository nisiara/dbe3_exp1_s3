package com.bancoxyz.transformation.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnualAccountInput {
  private Integer cuenta_id;
  private String fecha;
  private String transaccion;
  private Integer monto;
  private String descripcion;
}
