package com.bancoxyz.transformation.reader;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.bancoxyz.transformation.mapper.TransactionMapper;

import com.bancoxyz.transformation.model.input.TransactionInput;

/**
 * Lector personalizado que implementa ItemReader para leer transacciones desde un archivo CSV.
 * Esta clase encapsula un FlatFileItemReader de Spring Batch y proporciona la funcionalidad
 * de leer línea por línea del archivo transacciones.csv ubicado en el classpath.
 * 
 * Implementa ItemStream para manejar el ciclo de vida del archivo (abrir, cerrar, actualizar).
 */
@Component
public class TransactionItemReader  implements ItemReader<TransactionInput>, ItemStream {

  private final FlatFileItemReader<TransactionInput> transactionReader;

  /**
   * Constructor que configura el FlatFileItemReader interno.
   * Establece la configuración para:
   * - Nombre del lector
   * - Ubicación del archivo CSV (classpath:data/transacciones.csv)
   * - Omitir la primera línea (headers)
   * - Formato delimitado por comas
   * - Nombres de los campos: id, fecha, monto, tipo
   * - Tipo objetivo: TransactionInput.class
   * - Modo no estricto para continuar en caso de errores
   */
  public TransactionItemReader() {
    // Configura el FlatFileItemReader interno
    this.transactionReader = new FlatFileItemReaderBuilder<TransactionInput>()
      .name("transaccionesItemReader") // Define el nombre del lector
      .resource(new ClassPathResource("data/transacciones.csv")) // Ubicación del archivo CSV de entrada
      .linesToSkip(1) // Omite la primera línea (encabezados)
      .delimited() // Define el formato de los datos como delimitado
      .names("id", "fecha", "monto", "tipo") // Define los nombres de los campos
      .fieldSetMapper(new TransactionMapper())
      //.targetType(TransactionInput.class) // Clase objetivo para el mapeo de los datos
      .build();
    this.transactionReader.setStrict(false); // Permite continuar en caso de error en el archivo  
  }

  /**
   * Lee el siguiente elemento del archivo CSV.
   * Este método es llamado repetidamente por Spring Batch hasta que retorna null,
   * indicando que no hay más elementos para leer.
   * 
   * @return TransactionInput siguiente transacción leída del archivo, o null si no hay más
   * @throws Exception si ocurre un error durante la lectura
   */
  @Override
    public TransactionInput read() throws Exception {
      return transactionReader.read(); // Delegado para la lectura de cada ítem
    }

    /**
     * Abre el recurso de archivo antes de comenzar a leer.
     * Este método es llamado por Spring Batch antes de iniciar la lectura del step.
     * 
     * @param executionContext Contexto de ejecución que puede contener estado del step
     * @throws ItemStreamException si hay problemas al abrir el archivo
     */
    @Override
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
      transactionReader.open(executionContext); // Abre el lector delegado
    }

    /**
     * Cierra el recurso de archivo después de completar la lectura.
     * Este método es llamado por Spring Batch al finalizar el step.
     * 
     * @throws ItemStreamException si hay problemas al cerrar el archivo
     */
    @Override
    public void close() throws ItemStreamException {
       transactionReader.close(); // Cierra el lector delegado
    }

    /**
     * Actualiza el ExecutionContext con el estado actual del lector.
     * Este método permite que Spring Batch guarde el progreso de lectura
     * para permitir reinicio en caso de falla.
     * 
     * @param executionContext Contexto donde se guarda el estado actual
     * @throws ItemStreamException si hay problemas al actualizar el estado
     */
    @Override
    public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
      transactionReader.update(executionContext);
    }

  

}
