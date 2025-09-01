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
    
    // Manejar cuenta_id que puede estar vacío
    try {
      input.setCuenta_id(fieldSet.readInt("cuenta_id"));
    } catch (Exception e) {
      input.setCuenta_id(0); // Valor por defecto para que el processor lo maneje
    }

    // Manejar saldo que puede estar vacío o nulo
    try {
      input.setSaldo(fieldSet.readInt("saldo"));
    } catch (Exception e) {
      input.setSaldo(null); // Si no se puede leer, setear como null para que el processor lo maneje
    }
    
    // Manejar edad que puede estar vacía
    try {
      input.setEdad(fieldSet.readInt("edad"));
    } catch (Exception e) {
      input.setEdad(0); // Valor por defecto para que el processor lo maneje
    }
    
    input.setNombre(fieldSet.readString("nombre"));
    input.setTipo(fieldSet.readString("tipo"));

    return input;
  }
}
