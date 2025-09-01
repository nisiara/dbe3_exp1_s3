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

@Component
public class TransactionItemReader  implements ItemReader<TransactionInput>, ItemStream {

  private final FlatFileItemReader<TransactionInput> transactionReader;

  public TransactionItemReader() {
    this.transactionReader = new FlatFileItemReaderBuilder<TransactionInput>()
      .name("transaccionesItemReader") 
      .resource(new ClassPathResource("data/transacciones.csv"))
      .linesToSkip(1) 
      .delimited() 
      .names("id", "fecha", "monto", "tipo") 
      .fieldSetMapper(new TransactionMapper())
      .build();
    this.transactionReader.setStrict(false);
  }

  @Override
  public TransactionInput read() throws Exception {
    return transactionReader.read();
  }

  @Override
  public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    transactionReader.open(executionContext);
  }

  @Override
  public void close() throws ItemStreamException {
    transactionReader.close(); 
  }

  @Override
  public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    transactionReader.update(executionContext);
  }

}
