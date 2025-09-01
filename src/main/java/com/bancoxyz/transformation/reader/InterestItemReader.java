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

import com.bancoxyz.transformation.mapper.InterestMapper;
import com.bancoxyz.transformation.model.input.InterestInput;

@Component
public class InterestItemReader implements ItemReader<InterestInput>, ItemStream {
  
  private final FlatFileItemReader<InterestInput> interestReader;

  public InterestItemReader() {
    this.interestReader = new FlatFileItemReaderBuilder<InterestInput>()
      .name("interesesItemReader") 
      .resource(new ClassPathResource("data/intereses.csv"))
      .linesToSkip(1)
      .delimited()
      .names("cuenta_id", "nombre", "saldo", "edad", "tipo") 
      .fieldSetMapper(new InterestMapper())
      .build();
    this.interestReader.setStrict(false); 
  }

  @Override
  public InterestInput read() throws Exception {
    return interestReader.read(); 
  }

  @Override
  public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    interestReader.open(executionContext);
  }
    
  @Override
  public void close() throws ItemStreamException {
    interestReader.close();
  }

  @Override
  public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    interestReader.update(executionContext);
  }

}
