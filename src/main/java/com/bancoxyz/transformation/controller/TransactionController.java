package com.bancoxyz.transformation.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch")
public class TransactionController {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job transactionJob;

	@Autowired
	private Job interestJob;

	@Autowired
	private Job annualAccountJob;

	@PostMapping("/transaction-job")
	public String runTransactionJob() {
		try {
			// Crear parámetros únicos para evitar restricciones de re-ejecución
			JobParameters jobParameters = new JobParametersBuilder()
					.addLong("time", System.currentTimeMillis())
					.toJobParameters();

			jobLauncher.run(transactionJob, jobParameters);
			return "Batch transacciones.csv ejecutado";
		} catch (Exception e) {
			return "Error al ejecutar el batch: " + e.getMessage();
		}
	}

	@PostMapping("/interest-job")
	public String runInterestJob() {
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(interestJob, jobParameters);
			return "Batch intereses.csv ejecutado";
		} catch (Exception e) {
			return "Error al ejecutar el batch: " + e.getMessage();
		}
	}

	@PostMapping("/annual-account-job")
	public String runAnnualAccountJob() {
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(annualAccountJob, jobParameters);
			return "Batch cuentas-anuales.csv ejecutado";
		} catch (Exception e) {
			return "Error al ejecutar el batch: " + e.getMessage();
		}
	}

}
