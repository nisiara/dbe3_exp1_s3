package com.bancoxyz.transformation.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.lang.NonNull;

/**
 * Listener alternativo para el job de transacciones (actualmente no utilizado).
 * Esta clase implementa JobExecutionListener similar a JobCompletionTransactionListener
 * pero con funcionalidad más básica, solo registrando el inicio y fin del job
 * sin realizar acciones adicionales como escribir archivos de errores.
 * 
 * NOTA: Esta clase está comentada en la configuración del batch y no se está utilizando
 * actualmente en el flujo principal.
 */
public class TransactionJobListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionJobListener.class);

    /**
     * Se ejecuta antes de iniciar el job.
     * Simplemente registra información sobre el inicio del job.
     * 
     * @param jobExecution Contexto de ejecución del job
     */
    @Override
    public void beforeJob(@NonNull JobExecution jobExecution) {
        logger.info("Inicio del Job: {}", jobExecution.getJobInstance().getJobName());
    }

    /**
     * Se ejecuta después de completar el job.
     * Simplemente registra información sobre la finalización del job.
     * 
     * @param jobExecution Contexto de ejecución del job con el estado final
     */
    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        logger.info("Fin del Job: {}", jobExecution.getJobInstance().getJobName());
    }
}