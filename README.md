# 🎫 Ticket Backend

API REST para un sistema de gestión de tickets de soporte, construida con **Spring Boot 4** y **Java 21**. Autenticación stateless con JWT, autorización por roles y persistencia en PostgreSQL.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-black?logo=jsonwebtokens&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## Descripción

Backend de un sistema tipo *helpdesk*: los usuarios se registran, inician sesión y gestionan tickets de soporte (crear, listar, actualizar, cambiar estado y eliminar). Pensado como API para un frontend en Angular.

## Features

- **Autenticación JWT** — login sin sesiones ni cookies, stateless.
- **Roles de usuario** — `ADMIN`, `AGENT`, `USER`.
- **CRUD completo de tickets** — con estado, prioridad, asignación y creador.
- **Validación de datos** — Bean Validation (`jakarta.validation`) en todos los DTOs de entrada.
- **Manejo global de errores** — respuestas de error consistentes en toda la API.
- **Passwords hasheadas** — BCrypt, nunca se persisten en texto plano.
- **CORS configurado** — listo para consumir desde un frontend Angular en `localhost:4200`.

## Stack

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Seguridad | Spring Security + JWT (`jjwt` 0.12.6) |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | PostgreSQL 16 |
| Build | Maven |
| Utilidades | Lombok |

## Arquitectura

```
src/main/java/dev/dangonzalez/ticket_backend/
├── config/          # Configuración de Spring Security (JWT, CORS, rutas públicas/privadas)
├── controller/       # Endpoints REST (Auth, Ticket)
├── domain/           # Entidades JPA (User, Ticket) y enums (Role, TicketStatus, TicketPriority)
│   └── dto/           # Records de entrada/salida (nunca se exponen las entidades directamente)
├── exception/        # Excepciones custom + manejador global (@ControllerAdvice)
├── mapper/            # Entidad ↔ DTO
├── repository/        # Interfaces de Spring Data JPA
├── security/           # Filtro JWT, UserDetailsService, handlers de 401/403
└── service/            # Lógica de negocio
```

## Empezar

### Requisitos

- Java 21+
- Maven (o usar el wrapper `./mvnw` incluido)
- Docker (para levantar PostgreSQL) o una instancia propia de PostgreSQL 16

### 1. Cloná el repo

```bash
git clone https://github.com/danielgonzalez57/Backend-Springboot.git
cd Backend-Springboot
```

### 2. Levantá la base de datos

```bash
docker compose up -d
```

Esto levanta PostgreSQL en `localhost:5432` con la base `ticket_db`.

### 3. Configurá las variables de entorno (opcional)

La app funciona out-of-the-box con valores por defecto para desarrollo local. Para sobreescribirlos:

| Variable | Descripción | Default (dev) |
|---|---|---|
| `DB_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Password de PostgreSQL | `postgres` |
| `JWT_SECRET` | Clave secreta para firmar los JWT | placeholder de desarrollo |

> **Importante:** antes de desplegar a cualquier entorno real, definí un `JWT_SECRET` propio (por ejemplo con `openssl rand -base64 64`) y credenciales de base de datos que no sean las de desarrollo.

### 4. Ejecutá la aplicación

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/v1`.

## Endpoints

### Auth (`/api/v1/auth`) — públicos

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/register` | Registra un nuevo usuario |
| `POST` | `/auth/login` | Inicia sesión y devuelve un JWT |

**POST `/auth/register`**

```json
// Request
{
  "name": "Daniel Gonzalez",
  "email": "daniel@test.com",
  "password": "password123",
  "role": "ADMIN"
}
```

```json
// Response 201
{
  "id": 1,
  "name": "Daniel Gonzalez",
  "email": "daniel@test.com",
  "role": "ADMIN",
  "createdAt": "2026-08-12T10:00:00"
}
```

**POST `/auth/login`**

```json
// Request
{ "email": "daniel@test.com", "password": "password123" }
```

```json
// Response 200
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "user": { "id": 1, "name": "Daniel Gonzalez", "email": "daniel@test.com", "role": "ADMIN", "createdAt": "2026-08-12T10:00:00" }
}
```

### Tickets (`/api/v1/tickets`) — requieren JWT

Todas las rutas necesitan el header `Authorization: Bearer <token>`.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/tickets` | Crea un ticket (nace en estado `OPEN`) |
| `GET` | `/tickets` | Lista todos los tickets |
| `GET` | `/tickets/{id}` | Obtiene un ticket por id |
| `PUT` | `/tickets/{id}` | Actualiza un ticket |
| `PATCH` | `/tickets/{id}/status` | Actualiza únicamente el estado |
| `DELETE` | `/tickets/{id}` | Elimina un ticket |

**POST `/tickets`**

```json
// Request
{
  "title": "No puedo iniciar sesión",
  "description": "Me tira error 500 al loguearme",
  "priority": "URGENT",
  "assignedTo": 2,
  "createdBy": 1
}
```

```json
// Response 201
{
  "id": 5,
  "title": "No puedo iniciar sesión",
  "description": "Me tira error 500 al loguearme",
  "status": "OPEN",
  "priority": "URGENT",
  "assignedTo": { "id": 2, "name": "Agente Uno", "email": "agente@test.com", "role": "AGENT", "createdAt": "..." },
  "createdBy": { "id": 1, "name": "Daniel Gonzalez", "email": "daniel@test.com", "role": "ADMIN", "createdAt": "..." }
}
```

**PATCH `/tickets/{id}/status`**

```json
// Request
{ "status": "RESOLVED" }
```

**Valores posibles**

- `status`: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`
- `priority`: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- `role`: `ADMIN`, `AGENT`, `USER`

### Formato de error

Todas las respuestas de error siguen esta misma estructura:

```json
{
  "timestamp": "2026-08-12T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket con id 99 no encontrado",
  "path": "/api/v1/tickets/99"
}
```

## Colección de pruebas

En `ticket-backend-api/` hay una colección [Bruno](https://www.usebruno.com/) con requests listos para probar los endpoints de Auth y Tickets. La carpeta `environments/` (con variables como el token de sesión) está excluida del repo por contener datos locales.

## Licencia

MIT
