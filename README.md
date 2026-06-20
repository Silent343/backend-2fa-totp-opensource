# Auth Starter Kit — Spring Boot

Migración del backend original (Node.js/Express/TypeScript) a **Spring Boot 3 + Java 21**.
Misma arquitectura DDD/hexagonal, misma lógica de negocio, mismo contrato de API — ahora en Java con Swagger UI integrado.

## Stack

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 3.3 + Spring Security |
| Lenguaje | Java 21 (records, pattern matching) |
| Persistencia | Spring Data JPA + Flyway |
| BD (dev) | H2 en memoria (equivalente al SQLite original) |
| BD (prod) | PostgreSQL |
| JWT | JJWT 0.12 |
| TOTP | GoogleAuthenticator |
| QR Code | ZXing |
| Docs API | SpringDoc OpenAPI (Swagger UI) |
| Contenedor | Docker (multi-stage) |

## Arrancar en dev (H2, sin Docker)

```bash
# Requiere Java 21 + Maven 3.9
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Swagger UI disponible en: http://localhost:8080/swagger-ui.html  
H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:authkit`, user: `sa`)

## Arrancar en prod (Docker Compose con PostgreSQL)

```bash
# Edita los secrets en docker-compose.yml antes de ejecutar
docker compose up --build
```

## Endpoints

### Públicos
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Crear cuenta |
| POST | `/auth/login` | Login (paso 1) |
| POST | `/auth/login/totp` | Verificar código TOTP (paso 2) |
| POST | `/auth/refresh` | Renovar tokens |
| GET | `/health` | Health check |

### Protegidos (requieren `Authorization: Bearer <access_token>`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/auth/me` | Perfil del usuario |
| POST | `/auth/logout` | Cerrar sesión |
| POST | `/auth/totp/setup` | Iniciar setup 2FA → QR + secret |
| POST | `/auth/totp/enable` | Confirmar activación 2FA |
| POST | `/auth/totp/disable` | Desactivar 2FA |

## Arquitectura (DDD/Hexagonal)

```
src/main/java/com/authkit/
├── domain/                     ← Dominio puro (sin frameworks)
│   ├── model/User.java         ← Entidad de dominio
│   ├── valueobject/            ← Email.java, Password.java
│   ├── repository/             ← Puerto UserRepository (interface)
│   └── exception/              ← DomainException y subclases
│
├── application/                ← Casos de uso
│   ├── dto/AuthDtos.java       ← Requests / Responses (records)
│   └── usecase/                ← RegisterUser, Login, TOTP, Refresh, Logout...
│
└── infrastructure/             ← Adapters
    ├── persistence/            ← JPA entities, mappers, repositories
    ├── security/               ← JWT, BCrypt, TOTP, QR Code (implementaciones)
    ├── config/                 ← SecurityConfig, OpenApiConfig
    └── web/
        ├── controller/         ← AuthController (con anotaciones Swagger)
        ├── filter/             ← JwtAuthFilter
        └── advice/             ← GlobalExceptionHandler
```

## Variables de entorno

| Variable | Default (dev) | Descripción |
|---|---|---|
| `JWT_ACCESS_SECRET` | `dev-access-secret-change-me-32chars!!` | Secret para access tokens (mín. 32 chars) |
| `JWT_REFRESH_SECRET` | `dev-refresh-secret-change-me-32chars!` | Secret para refresh tokens |
| `JWT_TOTP_CHALLENGE_SECRET` | `dev-totp-chal-secret-change-me-32ch` | Secret para totp challenge tokens |
| `JWT_ACCESS_EXPIRES_IN` | `900` | Expiración access token (segundos, 15 min) |
| `JWT_REFRESH_EXPIRES_IN` | `604800` | Expiración refresh token (7 días) |
| `JWT_TOTP_EXPIRES_IN` | `300` | Expiración TOTP challenge (5 min) |
| `TOTP_ISSUER` | `Auth Starter Kit` | Nombre del emisor en el QR |
| `CORS_ORIGIN` | `http://localhost:4200` | Origen permitido en CORS |
| `DATABASE_URL` | H2 (dev) / `jdbc:postgresql://...` (prod) | URL de conexión a la BD |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | Credenciales BD (solo perfil prod) |

## Reglas de contraseña (igual que el original)
- Mínimo 8 caracteres
- Al menos una mayúscula
- Al menos un número

## Notas de la migración Node → Spring

- **Prisma → JPA + Flyway**: esquema definido en SQL (`V1__create_users_table.sql`), no en DSL propietario.
- **express-validator → Jakarta Bean Validation**: `@NotBlank`, `@Email`, `@Size` en los DTOs.
- **jsonwebtoken → JJWT 0.12**: API fluida, verificación con `Jwts.parser().verifyWith(key)`.
- **otplib → GoogleAuthenticator**: misma lógica TOTP RFC 6238, diferente librería.
- **qrcode (npm) → ZXing**: genera el mismo PNG base64 como data URL.
- **Perfiles de Spring**: `dev` usa H2 en memoria (igual que SQLite, cero config); `prod` usa PostgreSQL.
