package com.bancoxyz.transformation.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindException;

import com.bancoxyz.transformation.model.input.AnnualAccountInput;

public class AnnualAccountMapper implements FieldSetMapper<AnnualAccountInput>{
  
  @Override
  @NonNull
  public AnnualAccountInput mapFieldSet(@NonNull FieldSet fieldSet) throws BindException {
    
    AnnualAccountInput input = new AnnualAccountInput();
    input.setCuenta_id(fieldSet.readInt("cuenta_id"));
    input.setFecha(fieldSet.readString("fecha")); // Mapeo directo como String
    
    // Manejar monto que puede estar vacío o nulo
    try {
      input.setMonto(fieldSet.readInt("monto"));
    } catch (Exception e) {
      input.setMonto(null); // Si no se puede leer, setear como null para que el processor lo maneje
    }
    
    input.setTransaccion(fieldSet.readString("transaccion"));
    input.setDescripcion(fieldSet.readString("descripcion"));

    return input;
  }
}
