# Reserva Service - Sistema de Reservas de Hoteles

Microservicio de gestión de reservas, clientes y dashboard. Publica eventos a Kafka para notificaciones y se comunica con auth-service y hotel-service.

## Información del Servicio

| Propiedad | Valor |
|-----------|-------|
| Puerto | 8083 |
| Java | 21 |
| Spring Boot | 3.5.7 |
| Spring Cloud | 2024.0.1 |
| Context Path | /api/v1 |
| Base de Datos | MySQL |
| Message Broker | Kafka (Producer) |

## Estructura del Proyecto

```
ms-reserva/
└── reserva-service/
    ├── pom.xml
    ├── contracts/
    │   └── reserva-service-api.yaml
    └── src/main/
        ├── java/com/hotel/reserva/
        │   ├── ReservaServiceApplication.java
        │   ├── api/
        │   │   ├── ReservasController.java
        │   │   ├── ClientesController.java
        │   │   ├── MisReservasController.java
        │   │   └── DashboardController.java
        │   ├── core/
        │   │   ├── cliente/ (model, repository, service)
        │   │   ├── reserva/ (model, repository, service)
        │   │   ├── detalle_reserva/ (model, repository)
        │   │   └── dashboard/ (service)
        │   ├── helpers/ (exceptions, mappers)
        │   ├── infrastructure/
        │   │   ├── config/ (RabbitConfig, CorsConfig)
        │   │   ├── events/ (ReservaNotificationPublisher)
        │   │   └── security/ (AuthContextFilter)
        │   └── internal/
        │       ├── HotelInternalApi.java
        │       ├── AuthInternalApi.java
        │       └── dto/
        └── resources/
            └── application.yml
```

## Endpoints

### Reservas (Público + Admin)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/reservas` | Crear reserva | No |
| GET | `/api/v1/reservas/{id}` | Obtener por ID | No |
| POST | `/api/v1/reservas/{id}/confirmar-pago` | Confirmar pago | No |
| POST | `/api/v1/reservas/{id}/cancelar` | Cancelar | No |
| GET | `/api/v1/reservas` | Listar (admin) | ADMIN |
| PATCH | `/api/v1/reservas/{id}` | Actualizar (admin) | ADMIN |
| DELETE | `/api/v1/reservas/{id}` | Eliminar | ADMIN |

### Clientes (Admin)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/clientes` | Listar clientes | ADMIN |
| GET | `/api/v1/clientes/{id}` | Obtener por ID | ADMIN |
| GET | `/api/v1/clientes/dni/{dni}` | Buscar por DNI | ADMIN |

### Mis Reservas (Usuario)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/mis-reservas` | Listar mis reservas | JWT |
| GET | `/api/v1/mis-reservas/{id}` | Obtener mi reserva | JWT |
| PATCH | `/api/v1/mis-reservas/{id}` | Actualizar fechas | JWT |

### Dashboard (Admin)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/dashboard/estadisticas` | Estadísticas | ADMIN |

## Variables de Entorno

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SERVER_PORT` | Puerto del servicio | `8083` |
| `SPRING_DATASOURCE_URL` | URL MySQL | `jdbc:mysql://mysql:3306/reserva_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario BD | `hotel_user` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña BD | `hotel_pass` |
| `SPRING_RABBITMQ_HOST` | Host RabbitMQ | `rabbitmq` |
| `SPRING_RABBITMQ_PORT` | Puerto RabbitMQ | `5672` |
| `KAFKA_BOOTSTRAP_SERVERS` | Servidores Kafka | `kafka:29092` |
| `KAFKA_RESERVA_NOTIFICATIONS_TOPIC` | Topic Kafka | `reserva.notifications` |
| `EUREKA_URL` | URL Eureka | `http://discovery-service:8761/eureka` |
| `CONFIG_SERVER_URL` | URL Config Server | `http://config-server:8888` |
| `AUTH_SERVICE_URL` | URL Auth Service | `http://auth-service:8081` |
| `HOTEL_SERVICE_URL` | URL Hotel Service | `http://hotel-service:8082` |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS | `http://localhost:4200` |

---

## Docker

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
COPY contracts ./contracts

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/reserva-service-*.jar app.jar

EXPOSE 8083

ENV JAVA_OPTS="-Xms256m -Xmx512m"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/api/v1/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  reserva-service:
    build:
      context: ./reserva-service
      dockerfile: Dockerfile
    container_name: reserva-service
    ports:
      - "8083:8083"
    environment:
      - SERVER_PORT=8083
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/reserva_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=hotel_user
      - SPRING_DATASOURCE_PASSWORD=hotel_pass
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_PORT=5672
      - SPRING_RABBITMQ_USERNAME=guest
      - SPRING_RABBITMQ_PASSWORD=guest
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092
      - KAFKA_RESERVA_NOTIFICATIONS_TOPIC=reserva.notifications
      - EUREKA_URL=http://discovery-service:8761/eureka
      - CONFIG_SERVER_URL=http://config-server:8888
      - AUTH_SERVICE_URL=http://auth-service:8081
      - HOTEL_SERVICE_URL=http://hotel-service:8082
      - CORS_ALLOWED_ORIGINS=http://localhost:4200
    depends_on:
      mysql:
        condition: service_healthy
      kafka:
        condition: service_started
      hotel-service:
        condition: service_healthy
    networks:
      - hotel-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8083/api/v1/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 90s
    restart: unless-stopped

networks:
  hotel-network:
    external: true
```

### Comandos Docker

```bash
# Compilar
cd reserva-service
./mvnw clean package -DskipTests

# Construir imagen
docker build -t reserva-service:latest ./reserva-service

# Ejecutar
docker run -d \
  --name reserva-service \
  -p 8083:8083 \
  -e SERVER_PORT=8083 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/reserva_db \
  -e SPRING_DATASOURCE_USERNAME=hotel_user \
  -e SPRING_DATASOURCE_PASSWORD=hotel_pass \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
  -e AUTH_SERVICE_URL=http://auth-service:8081 \
  -e HOTEL_SERVICE_URL=http://hotel-service:8082 \
  -e EUREKA_URL=http://discovery-service:8761/eureka \
  --network hotel-network \
  reserva-service:latest

# Verificar
curl http://localhost:8083/api/v1/actuator/health

# Crear reserva
curl -X POST http://localhost:8083/api/v1/reservas \
  -H "Content-Type: application/json" \
  -d '{
    "hotelId": 1,
    "fechaInicio": "2025-03-01",
    "fechaFin": "2025-03-05",
    "habitacionesIds": [1, 2],
    "cliente": {
      "nombre": "Juan",
      "apellido": "Pérez",
      "dni": "12345678",
      "email": "juan@test.com",
      "telefono": "987654321"
    }
  }'
```

---

## Kubernetes

### Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reserva-service
  namespace: hotel-system
  labels:
    app: reserva-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: reserva-service
  template:
    metadata:
      labels:
        app: reserva-service
    spec:
      containers:
        - name: reserva-service
          image: ${ACR_NAME}.azurecr.io/reserva-service:latest
          ports:
            - containerPort: 8083
          env:
            - name: SERVER_PORT
              value: "8083"
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:mysql://mysql:3306/reserva_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: hotel-secrets
                  key: mysql-user
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: hotel-secrets
                  key: mysql-password
            - name: SPRING_RABBITMQ_HOST
              value: "rabbitmq"
            - name: KAFKA_BOOTSTRAP_SERVERS
              value: "kafka:29092"
            - name: KAFKA_RESERVA_NOTIFICATIONS_TOPIC
              value: "reserva.notifications"
            - name: EUREKA_URL
              value: "http://discovery-service:8761/eureka"
            - name: AUTH_SERVICE_URL
              value: "http://auth-service:8081"
            - name: HOTEL_SERVICE_URL
              value: "http://hotel-service:8082"
            - name: CORS_ALLOWED_ORIGINS
              value: "https://tu-dominio.com"
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /api/v1/actuator/health/liveness
              port: 8083
            initialDelaySeconds: 90
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /api/v1/actuator/health/readiness
              port: 8083
            initialDelaySeconds: 60
            periodSeconds: 5
```

### Service

```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: reserva-service
  namespace: hotel-system
spec:
  type: ClusterIP
  selector:
    app: reserva-service
  ports:
    - port: 8083
      targetPort: 8083
      name: http
```

### Comandos Kubernetes

```bash
# Aplicar manifiestos
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Verificar
kubectl get pods -n hotel-system -l app=reserva-service
kubectl logs -f deployment/reserva-service -n hotel-system

# Port-forward
kubectl port-forward svc/reserva-service 8083:8083 -n hotel-system

# Test
curl http://localhost:8083/api/v1/actuator/health
```

---

## Azure

### 1. Construir y Subir a ACR

```bash
export ACR_NAME="acrhotelreservas"

az acr login --name $ACR_NAME

az acr build \
  --registry $ACR_NAME \
  --image reserva-service:v1.0.0 \
  --image reserva-service:latest \
  ./reserva-service
```

### 2. Deployment en AKS

```yaml
# k8s/azure-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reserva-service
  namespace: hotel-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: reserva-service
  template:
    metadata:
      labels:
        app: reserva-service
    spec:
      containers:
        - name: reserva-service
          image: acrhotelreservas.azurecr.io/reserva-service:v1.0.0
          ports:
            - containerPort: 8083
          env:
            - name: SERVER_PORT
              value: "8083"
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:mysql://mysql-hotel-reservas.mysql.database.azure.com:3306/reserva_db?useSSL=true"
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: hotel-secrets
                  key: mysql-user
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: hotel-secrets
                  key: mysql-password
            - name: KAFKA_BOOTSTRAP_SERVERS
              value: "eh-hotel-reservas.servicebus.windows.net:9093"
            - name: EUREKA_URL
              value: "http://discovery-service:8761/eureka"
            - name: AUTH_SERVICE_URL
              value: "http://auth-service:8081"
            - name: HOTEL_SERVICE_URL
              value: "http://hotel-service:8082"
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
```

### 3. Azure DevOps Pipeline

```yaml
# azure-pipelines.yml
trigger:
  branches:
    include:
      - main
  paths:
    include:
      - ms-reserva/**

variables:
  dockerRegistryServiceConnection: 'acr-connection'
  imageRepository: 'reserva-service'
  containerRegistry: 'acrhotelreservas.azurecr.io'
  dockerfilePath: 'ms-reserva/reserva-service/Dockerfile'
  tag: '$(Build.BuildId)'

pool:
  vmImage: 'ubuntu-latest'

stages:
  - stage: Build
    jobs:
      - job: Build
        steps:
          - task: Maven@3
            displayName: 'Maven Package'
            inputs:
              mavenPomFile: 'ms-reserva/reserva-service/pom.xml'
              goals: 'clean package'
              options: '-DskipTests'
              javaHomeOption: 'JDKVersion'
              jdkVersionOption: '1.21'

          - task: Docker@2
            displayName: 'Build and Push'
            inputs:
              command: buildAndPush
              repository: $(imageRepository)
              dockerfile: $(dockerfilePath)
              containerRegistry: $(dockerRegistryServiceConnection)
              tags: |
                $(tag)
                latest

  - stage: Deploy
    dependsOn: Build
    jobs:
      - deployment: Deploy
        environment: 'production'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: KubernetesManifest@0
                  inputs:
                    action: deploy
                    kubernetesServiceConnection: 'aks-connection'
                    namespace: hotel-system
                    manifests: |
                      ms-reserva/k8s/*.yaml
                    containers: |
                      $(containerRegistry)/$(imageRepository):$(tag)
```

---

## Eventos Publicados (Kafka)

El servicio publica eventos al topic `reserva.notifications`:

| Evento | Trigger | Datos |
|--------|---------|-------|
| CREATED | Nueva reserva | reservaId, cliente, hotel, habitaciones, total |
| CONFIRMED | Pago confirmado | reservaId, estado |
| CANCELLED | Usuario cancela | reservaId, motivoCancelacion |
| CANCELLED_ADMIN | Admin cancela | reservaId, motivoCancelacion |

### Estructura del Evento

```java
public class ReservaNotificationEvent {
    String eventType;           // CREATED, CONFIRMED, CANCELLED, CANCELLED_ADMIN
    Long reservaId;
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

---

## Modelo de Datos

```
┌─────────────────┐       ┌─────────────────┐
│    Cliente      │       │     Reserva     │
├─────────────────┤       ├─────────────────┤
│ id              │ 1:N   │ id              │
│ dni             ├──────►│ fechaReserva    │
│ nombre          │       │ fechaInicio     │
│ apellido        │       │ fechaFin        │
│ email           │       │ total           │
│ telefono        │       │ estado          │
│ userId          │       │ hotelId         │
└─────────────────┘       │ hotelNombre     │
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

### Estados de Reserva

| Estado | Descripción |
|--------|-------------|
| PENDIENTE | Creada, esperando pago |
| CONFIRMADA | Pago confirmado |
| CANCELADA | Cancelada por usuario o admin |

---

## Flujo de Creación de Reserva

```
1. POST /api/v1/reservas
   │
   ├─► Validar request (campos, fechas)
   │
   ├─► Consultar hotel-service
   │   └─► GET /hoteles/{id}
   │   └─► GET /habitaciones/{id}
   │   └─► GET /habitaciones/{id}/disponibilidad
   │
   ├─► Crear/actualizar Cliente
   │
   ├─► Calcular total (precio × noches)
   │
   ├─► Guardar Reserva (PENDIENTE)
   │
   ├─► Publicar evento CREATED → Kafka
   │   └─► Topic: reserva.notifications
   │
   └─► Retornar ReservaCreatedResponse (201)
```

---

## Comunicación Inter-Servicios

### HotelInternalApi

```java
// Endpoints consultados en hotel-service
GET /api/v1/hoteles/{id}
GET /api/v1/habitaciones/{id}
GET /api/v1/habitaciones/{id}/disponibilidad?fechaInicio=...&fechaFin=...
GET /api/v1/habitaciones?hotelId=...&fechaInicio=...&fechaFin=...
```

### AuthInternalApi

```java
// Validación de token
POST /api/v1/auth/validate
```

---

## Dashboard Stats

El endpoint `/api/v1/dashboard/estadisticas` retorna:

```json
{
  "totalDepartamentos": 3,
  "totalHoteles": 10,
  "totalHabitaciones": 50,
  "totalReservas": 100,
  "reservasPendientes": 20,
  "reservasConfirmadas": 70,
  "reservasCanceladas": 10,
  "ingresosTotales": 50000.00,
  "hotelesPorDepartamento": [...],
  "reservasPorMes": [...],
  "ingresosPorMes": [...],
  "topHoteles": [...],
  "ultimasReservas": [...]
}
```

---

## Troubleshooting

```bash
# Ver logs
kubectl logs -f deployment/reserva-service -n hotel-system

# Verificar conexión con hotel-service
kubectl exec -it deployment/reserva-service -n hotel-system -- \
  wget -qO- http://hotel-service:8082/actuator/health

# Verificar Kafka producer
kubectl exec -it deployment/reserva-service -n hotel-system -- \
  wget -qO- http://localhost:8083/api/v1/actuator/health

# Debug reserva
curl http://localhost:8083/api/v1/reservas/1
```

---

## Ejecución Local

```bash
cd reserva-service

# Compilar
./mvnw clean package -DskipTests

# Ejecutar
java -jar target/reserva-service-1.0.0-SNAPSHOT.jar \
  --server.port=8083 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/reserva_db \
  --internal.auth-service.url=http://localhost:8081 \
  --internal.hotel-service.url=http://localhost:8082

# Swagger UI
open http://localhost:8083/api/v1/swagger-ui.html
```
