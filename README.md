# Desarrollo Backend 3 | Exp1-S3


## Objetivo del proyecto

En este proyecto de la semana 3 implementará un sistema de migración de datos en lotes utilizando Spring Batch, con el objetivo de modernizar el sistema legacy del Banco XYZ. El objetivo es procesar tlos siguientes archivos: `transacciones.csv`, `intereses.csv` y `cuenta-anuales.csv`.


Cada proceso tiene validaciones y manejo de errores junto con la transformación de fechas a un formato estandar, omisión de filas inválidas, tolerancia a fallos y procesamiento paralelo. 


Finalizado el proceso, los datos considerados correctos son escritos en una base de datos relacional utilizando MySQL y los datos inválidos son guardados en su respectivo archivo csv.


## 🛠️ Requisitos
- Java 21
- Maven 4.0
- Docker
- Dependencias
  - Spring Web
  - Spring Data JPA
  - MySql Driver
  - Spring Batch
  - Spring Boot Dev Tools
  - Lomkok


## 💾 Creación base de datos

#### Crea la imagen para la base de datos de MySQL a partir del archivo Dockerfile.
``` bash
docker build -t bancoxyz-db-image .
```


#### Ejecuta un contenedor a partir de la imagen creada con el puerto 3306.
``` bash
  docker run -d --name bancoxyz-container -p 3306:3306 bancoxyz-db-image
```


## ↔️ API Reference para porcesos batch.

#### Crear batch de archivo cuentas_anuales.csv
```bash
curl -X POST http://localhost:8080/batch/annual-account-job
```

#### Crear batch de archivo intereses.csv
```bash
curl -X POST http://localhost:8080/batch/interest-job
```

#### Crear batch de archivo transacciones.csv
```bash
curl -X POST http://localhost:8080/batch/transaction-job
```


## ✅ Revisar proceso en la Base de Datos

``` sql
SELECT * FROM tbl_transactions;
SELECT * FROM tbl_accounts;
SELECT * FROM tbl_interests;
```

## 🔗 Link
[![Github](https://img.shields.io/badge/github-000000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/nisiara/dbe3_exp1_s3.git)