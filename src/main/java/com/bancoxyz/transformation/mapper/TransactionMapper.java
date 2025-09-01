package com.bancoxyz.transformation.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindException;

import com.bancoxyz.transformation.model.input.TransactionInput;

/**
 * Mapper personalizado que implementa FieldSetMapper para convertir líneas del archivo CSV
 * en objetos TransactionInput. Este mapper lee los campos del FieldSet (que representa
 * una línea del CSV) y los mapea a los atributos del objeto TransactionInput.
 * 
 * Este mapper se enfoca únicamente en el mapeo básico de campos, delegando toda
 * la validación y conversión de tipos al TransactionItemProcessor.
 */
public class TransactionMapper implements FieldSetMapper<TransactionInput> {

  /**
   * Convierte un FieldSet (línea del archivo CSV) en un objeto TransactionInput.
   * Este método es llamado por Spring Batch para cada línea leída del archivo CSV,
   * mapeando los campos: id, fecha, monto, tipo a los atributos correspondientes
   * del objeto TransactionInput.
   * 
   * La fecha se mapea como String para permitir diferentes formatos que serán
   * procesados y validados posteriormente en el TransactionItemProcessor.
   * 
   * @param fieldSet Objeto que contiene los valores de una línea del CSV
   * @return TransactionInput objeto poblado con los datos de la línea
   * @throws BindException si hay problemas en el mapeo de los campos
   */
  @Override
  @NonNull
  public TransactionInput mapFieldSet(@NonNull FieldSet fieldSet) throws BindException {
    
    TransactionInput input = new TransactionInput();
    input.setId(fieldSet.readLong("id"));
    input.setFecha(fieldSet.readString("fecha")); // Mapeo directo como String
    
    // Manejar monto que puede estar vacío o nulo
    try {
      input.setMonto(fieldSet.readInt("monto"));
    } catch (Exception e) {
      input.setMonto(null); // Si no se puede leer, setear como null para que el processor lo maneje
    }
    
    input.setTipo(fieldSet.readString("tipo"));
    
    return input;
  }

}