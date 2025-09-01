package com.bancoxyz.transformation.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindException;

import com.bancoxyz.transformation.model.input.InterestInput;

public class InterestMapper implements FieldSetMapper<InterestInput>{
  
  @Override
  @NonNull
  public InterestInput mapFieldSet(@NonNull FieldSet fieldSet) throws BindException {
    
    InterestInput input = new InterestInput();
    
    try {
      input.setCuenta_id(fieldSet.readInt("cuenta_id"));
    } catch (Exception e) {
      input.setCuenta_id(0);
    }

    try {
      input.setSaldo(fieldSet.readInt("saldo"));
    } catch (Exception e) {
      input.setSaldo(null);
    }
    
    try {
      input.setEdad(fieldSet.readInt("edad"));
    } catch (Exception e) {
      input.setEdad(0);
    }
    
    input.setNombre(fieldSet.readString("nombre"));
    input.setTipo(fieldSet.readString("tipo"));

    return input;
  }
}
