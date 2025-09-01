package com.bancoxyz.transformation.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindException;

import com.bancoxyz.transformation.model.input.TransactionInput;


public class TransactionMapper implements FieldSetMapper<TransactionInput> {

  
  @Override
  @NonNull
  public TransactionInput mapFieldSet(@NonNull FieldSet fieldSet) throws BindException {
    
    TransactionInput input = new TransactionInput();
    input.setId(fieldSet.readLong("id"));
    input.setFecha(fieldSet.readString("fecha")); // Mapeo directo como String
    
    try {
      input.setMonto(fieldSet.readInt("monto"));
    } catch (Exception e) {
      input.setMonto(null); 
    }
    
    input.setTipo(fieldSet.readString("tipo"));
    
    return input;
  }

}