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
- [x] **HU-04 – Validar existencia y estado de cliente (interno)**
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

### 2. Iniciar Bases de Datos y Dozzle logs

- crear el archivo .docker/.env.development
- agregar lo siguiente para el usuario de postgres del contenedor docker

```
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
```

- Desde la raíz del proyecto, ejecuta:

```bash
docker compose up -d dozzle
docker compose up -d db
```

- abre tu navegado y accede a [http://localhost:9999](http://localhost:9999) para ver los logs en tiempo real de los contenedores
- en el caso de usar la terminal de postgres desde el contenedor docker puedes usar lo siguiente:

```bash
docker exec -it postgres-databases psql -U admin
```

- opcional levantar el projecto adminer para revisar y administrar las bases de datos

```bash
docker compose up -d adminer
```

- abre tu navegado y accede a [http://localhost:8080](http://localhost:8080)

### 3. Parar base de datos y volumenes

- en el caso de volver a iniciar bases de datos desde cero ejecute el siguiente comando

```bash
docker compose down -v
```

- este comando bajara a todos los servicios y tambien eliminara los volumnes, si no quieres eliminar los volumnes ejecuta el siguiente comando:

```bash
docker compose down
```

### 4. Ejecutar ms-customers
#### 4.1 Ejecutar con variables de entorno

- Crear el archivo **ms-customers/.env**

```
# spring boot variables
CUSTOMERS_SERVER_PORT=8081

# spring boot database variables
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB_CUSTOMERS=ms_customers_db
```

- Crear el archivo **ms-accounts/.env**

```
# spring boot variables
ACCOUNTS_SERVER_PORT=8082

# spring boot database variables
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB_ACCOUNTS=ms_accounts_db
```

#### 4.2 Ejecutar sin variables de entorno

- ejecucion para windows

```bash
./mvnw -pl ms-customers spring-boot:run
```

- ejecucion para linux

```bash
mvn -pl ms-customers spring-boot:run
```

#### 4.3 Compilacion y limpieza de dependencias

- resolucion de solo dependencias
```
mvn -pl ms-customers dependency:resolve
```

- limpieza y compilacion
```
mvn -pl ms-customers clean compile
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
