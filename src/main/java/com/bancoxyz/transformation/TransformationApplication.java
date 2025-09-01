package com.bancoxyz.transformation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransformationApplication {

	/**
	 * Método principal que inicia la aplicación Spring Boot.
	 * Esta aplicación está diseñada para procesar transacciones bancarias mediante Spring Batch,
	 * leyendo datos desde un archivo CSV, validándolos y guardándolos en una base de datos MySQL.
	 * 
	 * @param args Argumentos de línea de comandos pasados al iniciar la aplicación
	 */
	public static void main(String[] args) {
		SpringApplication.run(TransformationApplication.class, args);
	}

}
