# CodeBytes BankSystem - Sistema de Gestión Bancaria

[![Java](https://img.shields.io/badge/Java-17+-red?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/API-Docs-yellow?logo=swagger)](#)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?logo=openapiinitiative&logoColor=white)](#)
[![Docker](https://img.shields.io/badge/Docker-Compose-informational?logo=docker)](https://www.docker.com/)
[![Postman](https://img.shields.io/badge/Postman-FF6C37?logo=postman&logoColor=white)](#)

Sistema bancario basado en **arquitectura de microservicios** que se comunican entre sí para gestionar clientes y sus cuentas bancarias, permitiendo operaciones como transferencias, consultas de saldo y movimientos.

## 🚀 Estado del Proyecto
Actualmente, el proyecto se encuentra en su fase inicial de desarrollo.

- [x] **HU-01 - Registro de Cliente**
- [x] **HU-02 - Autenticación (Login)**
- [x] **HU-03 - Perfil de Cliente**
- [ ] **HU-04 – Validar existencia y estado de cliente (interno)**
- [ ] **HU-05 – Crear cuenta bancaria**
- [ ] **HU-06 – Listar cuentas del cliente**
- [ ] **HU-07 – Consultar detalle de una cuenta**
- [ ] **HU-08 – Depósito en cuenta**
- [ ] **HU-09 – Retiro de cuenta**
- [ ] **HU-10 – Transferencia entre cuentas**
- [ ] **HU-11 – Consultar historial de transacciones**
- [ ] **HU-12 – Manejo de errores entre microservicios**

## 📂 Estructura del Proyecto
El proyecto utiliza una estructura multi-módulo de Maven para separar las responsabilidades:

```text
bank-management-system/
├── ms-customers/      # Microservicio de Clientes y Seguridad (Puerto 8081)
│   └── src/main/java/com/codebytes5/banking/customers/
│      ├── config/
│      ├── controller/
│      ├── dto/
│      ├── enums/
│      ├── exception/
│      ├── mapper/
│      ├── model/
│      ├── repository/
│      ├── security/
│      └── service/
├── ms-accounts/       # Microservicio de Cuentas y Transacciones (Puerto 8082)
├── docker-compose.yml # Orquestación de bases de datos
└── BankSystem.postman_collection.json # Pruebas de API
```

## 🛠 Tecnologías Utilizadas
- **Lenguaje**: Java 17
- **Framework**: Spring Boot 3.5.10
- **Seguridad**: Spring Security + BCrypt
- **Base de Datos**: PostgreSQL (una instancia por microservicio)
- **Documentación**: OpenAPI 3.0 (Swagger UI)
- **Herramientas**: Docker, Maven, Lombok, MapStruct

## 🏁 Cómo Empezar

### 1. Requisitos previos
- Docker y Docker Compose instalados.
- Java 17+ instalado.

### 2. Iniciar Bases de Datos
Desde la raíz del proyecto, ejecuta:
```bash
docker-compose up -d
```
Esto levantará dos contenedores PostgreSQL:
- `customers_db` en el puerto `5433`.
- `accounts_db` en el puerto `5434`.

### 3. Ejecutar ms-customers
```bash
./mvnw -pl ms-customers spring-boot:run
```

## 🧪 Pruebas
Puedes probar el registro de clientes utilizando la colección de Postman incluida:
1. Importa `BankSystem.postman_collection.json` en Postman.
2. Ejecuta la petición **Register Customer**.
3. Ejecuta la petición **Login Customer**.
4. Ejecuta la petición **Get My Profile** (Con el token resultado de la petición anterior de Login).

## 📖 Documentación de API (OpenAPI)
Cada microservicio expone su propia documentación interactiva mediante Swagger UI.

- **ms-customers**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)