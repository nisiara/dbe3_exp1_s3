package com.bancoxyz.transformation.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestInput {
  private Integer cuenta_id;
  private String nombre;
  private Integer saldo;
  private Integer edad;
  private String tipo;
}
