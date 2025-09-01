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

import com.bancoxyz.transformation.mapper.AnnualAccountMapper;
import com.bancoxyz.transformation.model.input.AnnualAccountInput;

@Component
public class AnnualAccountItemReader implements ItemReader<AnnualAccountInput>, ItemStream {
  
  private final FlatFileItemReader<AnnualAccountInput> annualAccountReader;

  public AnnualAccountItemReader() {
    // Configura el FlatFileItemReader interno
    this.annualAccountReader = new FlatFileItemReaderBuilder<AnnualAccountInput>()
      .name("anualAccountItemReader") 
      .resource(new ClassPathResource("data/cuentas_anuales.csv"))
      .linesToSkip(1)
      .delimited()
      .names("cuenta_id", "fecha", "transaccion", "monto", "descripcion") 
      .fieldSetMapper(new AnnualAccountMapper())
      .build();
    this.annualAccountReader.setStrict(false); 
  }

  
  @Override
  public AnnualAccountInput read() throws Exception {
    return annualAccountReader.read();
  }

  @Override
  public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    annualAccountReader.open(executionContext);
  }
    
  @Override
  public void close() throws ItemStreamException {
      annualAccountReader.close();
  }
    
  @Override
  public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    annualAccountReader.update(executionContext);
  }
}
