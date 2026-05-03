# Reserva Service - Sistema de Reserva de Hoteles

Microservicio de gestión de reservas, clientes y dashboard. Publica eventos a Kafka (`reserva.notifications`) para que `notificacion-service` envíe correos al cliente. Se comunica con `auth-service` (validación de JWT y tokens técnicos) y `hotel-service` (consulta de hoteles, habitaciones y disponibilidad).

## Información del Servicio

| Propiedad | Valor |
|-----------|-------|
| Puerto | 8083 |
| Java | 21 |
| Spring Boot | 3.4.0 |
| Spring Cloud | 2024.0.1 |
| Context Path | `/api/v1` |
| Base de Datos | MySQL |
| Validación JWT | **RS256** con clave pública RSA compartida |
| Mensajería | **Kafka producer** (topic `reserva.notifications`) |

## Estructura del Proyecto (Package-by-Feature)

```
ms-reserva/
├── pom.xml
├── Dockerfile
├── env.example                ← copialo a .env y completalo en DEV
├── contracts/
│   └── reserva-service-api.yaml
└── src/main/
    ├── java/com/hotel/reserva/
    │   ├── ReservaServiceApplication.java
    │   ├── api/ (ReservasController, ClientesController, MisReservasController, DashboardController)
    │   ├── core/
    │   │   ├── cliente/ (model, repository, service)
    │   │   ├── reserva/ (model, repository, service)
    │   │   ├── detalle_reserva/ (model, repository)
    │   │   └── dashboard/ (service)
    │   ├── helpers/ (mappers, exceptions)
    │   ├── infrastructure/
    │   │   ├── config/ (SecurityConfig, JwtConfig, RestTemplateConfig)
    │   │   ├── events/ (ReservaNotificationEvent, ReservaNotificationPublisher)
    │   │   └── security/ (AuthContextFilter, AuthUtils)
    │   └── internal/
    │       ├── HotelInternalApi.java       ← cliente REST hacia hotel-service
    │       ├── ServiceTokenProvider.java   ← cachea token OAuth2 c.c.
    │       └── dto/
    └── resources/
        └── application.yml    ← bootstrap mínimo (config-server lo hidrata)
```

## Endpoints

> **Todas las rutas requieren JWT válido.** El `api-gateway` no las expone como públicas, y `SecurityConfig` declara `.anyRequest().authenticated()`. Las rutas administrativas exigen además el rol `ADMIN`.

### Reservas

| Método | Endpoint | Auth | Idempotency-Key |
|--------|----------|------|------------------|
| POST | `/api/v1/reservas` | JWT | **Requerido (UUID v4)** |
| GET | `/api/v1/reservas/{id}` | JWT | — |
| POST | `/api/v1/reservas/{id}/iniciar-pago` | JWT | **Requerido (UUID v4)** |
| POST | `/api/v1/reservas/{id}/confirmar` | JWT | — |
| POST | `/api/v1/reservas/{id}/cancelar` | JWT | — |
| GET | `/api/v1/reservas` (listar con filtros) | ADMIN |
| PATCH | `/api/v1/reservas/{id}` | ADMIN |
| DELETE | `/api/v1/reservas/{id}` | ADMIN |

### Mis Reservas (usuario autenticado)

| Método | Endpoint | Auth |
|--------|----------|------|
| GET | `/api/v1/mis-reservas` | JWT |
| GET | `/api/v1/mis-reservas/{id}` | JWT |
| PATCH | `/api/v1/mis-reservas/{id}` | JWT |

### Clientes

| Método | Endpoint | Auth |
|--------|----------|------|
| GET | `/api/v1/clientes` | ADMIN |
| GET | `/api/v1/clientes/{id}` | ADMIN |
| GET | `/api/v1/clientes/dni/{dni}` | ADMIN |

### Dashboard

| Método | Endpoint | Auth |
|--------|----------|------|
| GET | `/api/v1/dashboard/estadisticas` | ADMIN |

## Variables de Entorno

| Variable | Obligatoria | Descripción | Ejemplo (DEV) |
|----------|-------------|-------------|---------------|
| `CONFIG_IMPORT` | No | Import de Spring Cloud Config | `optional:configserver:http://localhost:8888` |
| `CONFIG_FAIL_FAST` | No | Falla rápido si config-server no responde | `false` (DEV) / `true` (PROD) |
| `SERVER_PORT` | No | Puerto HTTP (default 8083) | `8083` |
| `EUREKA_URL` | No | URL de Eureka (default `http://discovery-service:8761/eureka`) | `http://localhost:8761/eureka` |
| `SPRING_DATASOURCE_URL` | **Sí** | JDBC URL MySQL | `jdbc:mysql://localhost:3307/reserva_db` |
| `SPRING_DATASOURCE_USERNAME` | **Sí** | Usuario MySQL | - |
| `SPRING_DATASOURCE_PASSWORD` | **Sí** | Contraseña MySQL | - |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | No | Default `validate` (PROD-safe) | `update` (DEV) |
| `SPRING_JPA_SHOW_SQL` | No | Default `false` (PROD-safe) | `true` (DEV) |
| `KAFKA_BOOTSTRAP_SERVERS` | **Sí** | Brokers Kafka | `localhost:9092` |
| `KAFKA_RESERVA_NOTIFICATIONS_TOPIC` | No | Default `reserva.notifications` | `reserva.notifications` |
| `JWT_PUBLIC_KEY` | **Sí** | Clave pública RSA del auth-service (PEM 1 línea con `\n`) | - |
| `AUTH_SERVICE_URL` | **Sí** | Base URL del auth-service (sin context-path) | `http://localhost:8081` |
| `AUTH_SERVICE_CLIENT_ID` | **Sí** | Client ID asignado a reserva-service en auth | - |
| `AUTH_SERVICE_CLIENT_SECRET` | **Sí** | Client secret correspondiente | - |
| `HOTEL_SERVICE_URL` | **Sí** | Base URL del hotel-service | `http://localhost:8082` |
| `PAGO_SERVICE_URL` | **Sí** | Base URL del pago-service (SAGA orchestrator) | `http://localhost:8085` |
| `KAFKA_PAGO_EVENTS_TOPIC` | No | Default `pago.events` | `pago.events` |
| `KAFKA_RESERVA_CONSUMER_GROUP` | No | Group id consumer Kafka | `reserva-service` |
| `PAGO_DEFAULT_CURRENCY` | No | Moneda Stripe (default `USD`) | `USD` |
| `PAGO_SUCCESS_URL` | No | Redirect post-pago exitoso | `http://localhost:4200/reservas/success` |
| `PAGO_CANCEL_URL` | No | Redirect post-pago cancelado | `http://localhost:4200/reservas/cancel` |
| `JOBS_EXPIRAR_RESERVAS_FIXED_DELAY_MS` | No | Frecuencia del job de expiracion | `30000` |
| `JOBS_EXPIRAR_RESERVAS_INITIAL_DELAY_MS` | No | Delay inicial antes del primer run | `30000` |
| `CORS_ALLOWED_ORIGINS` | **Sí** | Origen permitido para CORS | `http://localhost:4200` |

> Las credenciales `AUTH_SERVICE_CLIENT_ID/SECRET` deben coincidir con `RESERVA_SERVICE_CLIENT_ID/SECRET` sembradas en `auth-service`.

## HikariCP Tuning (Ronda 7)

El connection pool default de Spring Boot 3 es HikariCP — excelente, pero con defaults conservadores. Tuneo aplicado a este servicio (y replicado en los otros 4 con DB):

| Propiedad | Valor | Razon |
|-----------|-------|-------|
| `maximum-pool-size` | 20 | Suficiente para 200 req/s con queries de <100ms. Mas no escala — saturariamos MySQL antes. |
| `minimum-idle` | 5 | Conexiones siempre warm — primer request post-idle no paga el costo de handshake. |
| `connection-timeout` | 5s | Si esperas mas de 5s por una conexion, la latencia P99 ya es inaceptable — mejor fallar rapido y devolver 503. |
| `max-lifetime` | 25min | Menor al `wait_timeout` default de MySQL (28800s = 8h) y a NATs/proxies que cortan ~30min idle. Forza reciclaje proactivo. |
| `idle-timeout` | 5min | Cierra idle por encima del `min-idle` cuando no hay carga. |
| `validation-timeout` | 2s | Health check rapido antes de entregar la conexion al caller. |
| `leak-detection-threshold` | 30s | Si una conexion no se devuelve al pool en 30s, log con stacktrace del adquirente — atrapa `try-finally` mal cerrados. |
| `keepalive-time` | 2min | Ping periodico para mantener viva la conexion atras de NATs/firewalls. |

**Por que importa**: sin estos, una falla del servicio bajo carga es indistinguible de un bug. Con el pool tuneado, Hikari te dice EXACTAMENTE que paso (timeout, leak, MySQL gone away).

## AtomicReference en ServiceTokenProvider (Ronda 7)

El cache del token tecnico OAuth2 (`client_credentials`) era `synchronized` — **bloquea TODOS los hilos** durante el HTTP call al auth-service. Bajo carga (50 reservas concurrentes = 200+ getToken() por segundo), eso era cuello de botella puro.

**Refactor con `AtomicReference<TokenSnapshot>` + `ReentrantLock`**:

```java
// Lectura: lock-free, multi-reader sin contention
TokenSnapshot snapshot = current.get();
if (snapshot != null && snapshot.isFresh()) return snapshot.token();

// Refresh: solo un thread (ReentrantLock), los demas reusan el snapshot
// "fresco" que dejo el primer thread (double-check inside the lock)
refreshLock.lock();
try {
    snapshot = current.get();
    if (snapshot.isFresh()) return snapshot.token();
    TokenSnapshot fresh = fetchFromAuth();
    current.set(fresh);
    return fresh.token();
} finally { refreshLock.unlock(); }
```

**Por que el record `TokenSnapshot`**: la inmutabilidad es la clave del patron. Todos los readers ven el mismo objeto consistente, sin sincronizacion explicita. Java garantiza visibilidad cross-thread de las assignments via `AtomicReference`.

**Impacto**: la lectura del token pasa de O(N) hilos serializados a O(1) lock-free. Solo cuando hay refresh (cada ~5min) se serializan brevemente.

## Outbox Pattern (Ronda 6)

Para evitar el problema clasico de **dual-write** (DB commit + Kafka publish son sistemas separados, no atomicos), todo evento de dominio se persiste primero en la tabla `outbox_event` dentro de la **misma transaccion** del cambio de estado. Un job asincronico lo publica a Kafka.

### Flujo

```
ReservaService.crearReserva (@Transactional):
  1. INSERT INTO reserva ...
  2. INSERT INTO habitacion_dia ...                   ┐
  3. INSERT INTO outbox_event (CREATED payload)       │ una sola tx
  4. COMMIT                                           ┘

OutboxRelayJob (@Scheduled fixedDelay=2s):
  SELECT * FROM outbox_event WHERE sent=0 ORDER BY id
    LIMIT 100 FOR UPDATE SKIP LOCKED
  → publica cada uno a Kafka (sincrono, .get())
  → UPDATE outbox_event SET sent=1, sent_at=NOW() WHERE id=...
```

### Garantias

| Propiedad | Resultado |
|-----------|-----------|
| Atomicidad cambio + evento | ✅ Si la tx hace rollback, NO queda evento |
| Kafka caido | ✅ Eventos se acumulan en outbox; al volver Kafka, el relay los entrega |
| App crashea entre INSERT y COMMIT | ✅ Tx rollback automatico |
| App crashea despues del COMMIT pero antes del relay | ✅ El proximo tick los toma |
| Multiples instancias del relay | ✅ `FOR UPDATE SKIP LOCKED` evita publicar duplicados |
| Reintentos de Kafka producer | ✅ `enable.idempotence=true` evita duplicados a nivel broker |
| Entrega final | **At-least-once** (consumers son idempotentes) |

### Producer Kafka idempotente

```yaml
enable.idempotence: true     # PID + sequence number
acks: all                    # Confirmacion del leader + ISRs
retries: Integer.MAX_VALUE   # No abandona ante errores transitorios
max.in.flight: 5             # Max permitido manteniendo idempotencia
```

### Cleanup

`OutboxCleanupJob` cron `0 0 3 * * *` borra `sent=true` con `sent_at < now-7d`. Mantiene una semana de eventos para forensics.

### Topics afectados

| Publisher | Topic | Destinatario |
|-----------|-------|--------------|
| `ReservaNotificationPublisher` → outbox → relay | `reserva.notifications` | `ms-notificacion-service` |

## Eventos Publicados (Kafka)

Topic: **`reserva.notifications`** (configurable). Key: `reservaId` (asegura ordenamiento por reserva).

| `eventType` | Trigger | Payload destacado |
|-------------|---------|-------------------|
| `CREATED` | Nueva reserva | `reservaId`, cliente, hotel, habitaciones, total, fechas |
| `CONFIRMED` | Pago aprobado (SAGA) | `reservaId`, estado |
| `PAYMENT_FAILED` | Pago rechazado (SAGA) | `reservaId`, motivoCancelacion |
| `EXPIRED` | Timeout de pago (job) | `reservaId`, estado |
| `CANCELLED` | Usuario cancela | `reservaId`, motivoCancelacion, fechaCancelacion |
| `CANCELLED_ADMIN` | Admin cancela | `reservaId`, motivoCancelacion, fechaCancelacion |

`notificacion-service` consume este topic y dispara correos al cliente.

## Eventos Consumidos (Kafka)

Topic: **`pago.events`** (publicado por `ms-pago`). Key: `reservaId`. Group id: `reserva-service`.

| `eventType` (publisher) | Accion en ms-reserva |
|-------------------------|----------------------|
| `PagoCreado` | Ignorado — la reserva ya esta en `PAGO_EN_PROCESO` |
| `PagoAprobado` | Transicion `PAGO_EN_PROCESO → CONFIRMADA`, limpia `expiresAt`, publica `CONFIRMED` |
| `PagoRechazado` | Transicion `PAGO_EN_PROCESO → PAGO_FALLIDO`, libera slots, publica `PAYMENT_FAILED` |

El listener (`PagoEventListener`) es **idempotente**: chequea el estado actual antes de transicionar. Esto cubre redelivery de Kafka, rebalance del consumer group y duplicados de webhook de Stripe.

### Estructura del evento

```java
public class ReservaNotificationEvent {
    String eventType;
    Long reservaId;
    Long userId;
    String clienteNombre;
    String clienteEmail;
    String hotelNombre;
    String hotelDireccion;
    String fechaInicio;
    String fechaFin;
    String fechaCancelacion;
    Double total;
    String estado;
    String motivoCancelacion;
    List<HabitacionDetalle> habitaciones;
}
```

## SAGA Orchestrator (Ronda 5.3)

`ms-reserva` es el **orquestador** de la SAGA de pagos. Coordina las transiciones de estado de la reserva con el resultado del cobro real en Stripe (vía `ms-pago`).

### Flujo completo

```
1. Cliente: POST /reservas (Idempotency-Key: <uuid>)
   ms-reserva → estado PENDIENTE_PAGO, expiresAt = now + 5min, slots reservados (UNIQUE)

2. Cliente: POST /reservas/{id}/iniciar-pago (Idempotency-Key: <uuid>)
   ms-reserva (Tx 1) → transicion a PAGO_EN_PROCESO + commit
   ms-reserva       → llama a ms-pago.crearPago (Resilience4j: CB + Retry)
   ms-pago          → crea Stripe Checkout Session, guarda Pago(PENDING)
   ms-reserva       → devuelve checkoutUrl al frontend
   * Si falla la llamada: Tx 2 compensatoria → revertir a PENDIENTE_PAGO

3. Cliente: redirige a Stripe Checkout, ingresa tarjeta, paga.

4a. Stripe → POST webhook ms-pago/pagos/webhook/stripe (verificacion HMAC)
    ms-pago → marca Pago APPROVED, publica PagoEvent("PagoAprobado") en pago.events
    ms-reserva (PagoEventListener) → transicion a CONFIRMADA, publica CONFIRMED

4b. Stripe → webhook con payment_failed
    ms-pago → marca Pago REJECTED, publica PagoEvent("PagoRechazado")
    ms-reserva → transicion a PAGO_FALLIDO + libera slots + publica PAYMENT_FAILED
```

### Compensacion por timeout

Si en el paso 2 la reserva queda en `PENDIENTE_PAGO` y el cliente nunca clickeo "Pagar":

```
ExpirarReservasJob (cada 30s):
  - Selecciona reservas con estado=PENDIENTE_PAGO y expiresAt < now
  - Transicion a EXPIRADA, libera slots, publica EXPIRED
```

**Importante**: el job NO toca `PAGO_EN_PROCESO`. Si el cliente esta pagando en Stripe en ese momento, la SAGA confia en el webhook (con retries de Stripe). Expirar `PAGO_EN_PROCESO` arriesgaria una race condition con un cliente cobrado sin reserva.

### Idempotency-Key (api-gateway)

Los endpoints `POST /reservas` y `POST /reservas/{id}/iniciar-pago` requieren el header `Idempotency-Key: <UUID v4>`. El **api-gateway** (no este servicio) hace la validacion contra Redis:

- Primera vez con esa key → ejecuta el request, cachea status + body 24h.
- Reintentos con misma key → devuelve la response cacheada (header `X-Idempotent-Replay: true`).
- Header faltante o invalido → `400 Bad Request`.
- Request en vuelo (SETNX hit pero sin response cacheada aun) → `409 Conflict`.

Esto evita doble cobro si el cliente reintenta por timeout de red.

## Comunicación Inter-Servicios

### `HotelInternalApi` (cliente REST hacia hotel-service)

```
GET /api/v1/hoteles/{id}
GET /api/v1/habitaciones/{id}
GET /api/v1/habitaciones/{id}/disponibilidad?fechaInicio=...&fechaFin=...
GET /api/v1/habitaciones?hotelId=...&fechaInicio=...&fechaFin=...
```

Las llamadas se autentican con un token técnico (OAuth2 `client_credentials`) emitido por `auth-service` y cacheado por `ServiceTokenProvider` (margen de 30s antes de expirar).

### Validación de JWT del usuario

`AuthContextFilter` extrae el JWT del header `Authorization`, valida con `JWT_PUBLIC_KEY` (RS256) y propaga el `userId` y rol al contexto de la request.

## Modelo de Datos

```
┌─────────────────┐       ┌─────────────────┐
│    Cliente      │       │     Reserva     │
├─────────────────┤       ├─────────────────┤
│ id              │ 1:N   │ id              │
│ dni             ├──────►│ fechaReserva    │
│ nombre, apellido│       │ fechaInicio     │
│ email, telefono │       │ fechaFin        │
│ userId          │       │ total, estado   │
└─────────────────┘       │ hotelId         │
                          │ cliente         │
                          └────────┬────────┘
                                   │ 1:N
                                   ▼
                          ┌─────────────────┐
                          │ DetalleReserva  │
                          ├─────────────────┤
                          │ id              │
                          │ habitacionId    │
                          │ precioNoche     │
                          │ reserva         │
                          └─────────────────┘
```

### Estados de Reserva (state machine)

| Estado | Descripción | Terminal |
|--------|-------------|---------|
| `PENDIENTE_PAGO` | Creada, esperando que el usuario pague (timeout 5 min via `expiresAt`) | No |
| `PAGO_EN_PROCESO` | El gateway de pago (Stripe) está procesando — esperando webhook | No |
| `CONFIRMADA` | Pago aprobado, reserva activa | No |
| `PAGO_FALLIDO` | Webhook de Stripe respondió RECHAZADO | Sí |
| `EXPIRADA` | Timeout sin pago (job de 5.3 detecta y libera slots) | Sí |
| `CANCELADA` | Cancelada por usuario o admin | Sí |

### Transiciones válidas

```
PENDIENTE_PAGO  → PAGO_EN_PROCESO | CANCELADA | EXPIRADA
PAGO_EN_PROCESO → CONFIRMADA | PAGO_FALLIDO | EXPIRADA
CONFIRMADA      → CANCELADA
PAGO_FALLIDO    → (terminal)
EXPIRADA        → (terminal)
CANCELADA       → (terminal)
```

Cualquier transición fuera de este set lanza `IllegalStateException` → HTTP 400. El admin **también** está sujeto a la state machine (decisión 2B).

### Concurrencia

- **`@Version` (optimistic locking)**: dos updates simultáneos sobre la misma reserva → el segundo recibe 409 `Concurrent Modification`.
- **Slot table `habitacion_dia`**: una fila por (habitacion_id, fecha) con UNIQUE compuesto. Dos `crearReserva` simultáneos para la misma habitación-fecha → la DB rechaza el segundo INSERT → 409 `HABITACION_FECHAS_OCUPADAS`. **No hay TOCTOU posible.**

## Flujo de Creación de Reserva

```
POST /api/v1/reservas (JWT del usuario)
   │
   ├─► AuthContextFilter valida JWT y extrae userId/rol
   ├─► Validar request (campos, fechas)
   ├─► ServiceTokenProvider.getToken() ─► auth-service /oauth/token (client_credentials)
   ├─► HotelInternalApi.getHotel(id)
   ├─► HotelInternalApi.checkDisponibilidad(habitacionId, fechas)
   ├─► Crear/actualizar Cliente
   ├─► Calcular total (precio × noches)
   ├─► Persistir Reserva (PENDIENTE)
   ├─► ReservaNotificationPublisher.publish(CREATED) ─► Kafka topic reserva.notifications
   └─► 201 ReservaCreatedResponse
```

## Seguridad

- **Validación JWT**: RS256 con `JWT_PUBLIC_KEY` (pareja pública de la privada del `auth-service`).
- **Sesiones**: STATELESS.
- **CORS**: deshabilitado en el servicio (lo maneja el `api-gateway`).
- **Todas las rutas requieren autenticación.** Las rutas administrativas exigen rol `ADMIN`.
- **Service-to-service**: token técnico via OAuth2 `client_credentials` cacheado con margen de 30s.

## Schema Migrations (Flyway)

El schema está versionado con **Flyway**. Cada cambio = nuevo script en `src/main/resources/db/migration/` con naming `V{n}__descripcion.sql`.

- `V1__init_schema.sql` — estado inicial: `cliente`, `reserva`, `detalle_reserva` con FKs e índices.
  - **Nota**: en V1 el `estado` todavía es `VARCHAR` y no existe la slot table `habitacion_dia`. Esos cambios llegan en `V2__estado_enum_y_slot_table.sql` (Ronda 5.1).
- Cambios futuros: `V2__...sql`, `V3__...sql`. **NUNCA se edita un script ya aplicado** — siempre se agrega uno nuevo.
- Flyway corre **antes** que Hibernate: aplica los scripts pendientes y luego Hibernate valida (`ddl-auto: validate`) que las entidades calzan con el schema.
- Tabla de control: `flyway_schema_history` (la crea Flyway al arrancar).

### Variables relevantes

| Variable | Default | Descripción |
|----------|---------|-------------|
| `SPRING_FLYWAY_ENABLED` | `true` | Activa/desactiva Flyway |
| `SPRING_FLYWAY_BASELINE_ON_MIGRATE` | `false` | `true` solo si la DB ya tenía tablas pre-Flyway |
| `SPRING_FLYWAY_VALIDATE_ON_MIGRATE` | `true` | Valida checksums de scripts ya aplicados |

### Workflow primera vez

1. Crear el schema vacío en MySQL: `CREATE DATABASE reserva_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. Levantar el servicio → Flyway aplica `V1` automáticamente.
3. Verificar con `SELECT version, description, success FROM flyway_schema_history;`.

## Ejecución Local (DEV)

```bash
# 1. Infra (MySQL + Kafka)
docker-compose -f docker-compose.infra.yml up -d

# 2. Variables
cp env.example .env
# editar .env (especialmente JWT_PUBLIC_KEY, AUTH_SERVICE_CLIENT_ID/SECRET, credenciales BD)

# 3. Levantar (auth-service y hotel-service deben estar arriba para tokens y consultas)
mvn spring-boot:run

# Swagger UI
open http://localhost:8083/api/v1/swagger-ui.html
```

## Ejecución en Docker (PROD)

Multi-stage build, JRE 21 alpine, usuario no-root, healthcheck en `/api/v1/actuator/health`. Se levanta como parte de `docker-compose.prod.yml` (a definir) consumiendo `.env.prod` con todas las variables marcadas como obligatorias.

## Troubleshooting

| Síntoma | Causa probable | Solución |
|---------|----------------|----------|
| `No se pudo obtener token tecnico` | auth-service caído o creds inválidas | Verificar `AUTH_SERVICE_URL` y que las creds coincidan con lo sembrado en auth |
| Errores Kafka al publicar | Kafka caído | `docker-compose -f docker-compose.infra.yml ps`; revisar `KAFKA_BOOTSTRAP_SERVERS` |
| 401 en endpoints | `JWT_PUBLIC_KEY` no es pareja de la privada del auth | Regenerar keypair y propagarlo a auth + gateway + servicios |
| `Could not resolve placeholder ...` | Falta env var obligatoria | Revisar la tabla |
| Llamadas a hotel-service fallan | hotel-service caído o `HOTEL_SERVICE_URL` mal | Verificar Eureka y que hotel-service esté UP |
