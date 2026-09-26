# ride-finderApp

A Spring Boot microservices demo for finding the **nearest available driver** to a rider using geospatial search and event-driven communication.

The project demonstrates how a ride-hailing backend can be broken into independent microservices using **Spring Boot, Redis GEO, Apache Kafka, and PostgreSQL**.

## Architecture

The application consists of three microservices:

```text
                    ┌─────────────────┐
                    │   Ride Service  │
                    │                 │
                    │ Creates Ride    │
                    │ Requests       │
                    └────────┬────────┘
                             │
                             │ RideRequestedEvent
                             ▼
                       ┌───────────┐
                       │   Kafka   │
                       └─────┬─────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ Matching Service│
                    │                 │
                    │ Finds nearest   │
                    │ available driver│
                    └────────┬────────┘
                             │
                             │ Feign
                             ▼
                    ┌─────────────────┐
                    │ Location Service│
                    │                 │
                    │ Redis GEO       │
                    │ Driver locations│
                    └─────────────────┘
```

##  Microservices

### 1. Ride Service

Responsible for managing ride requests.

Responsibilities:

- Create ride requests
- Store ride information
- Publish `RideRequestedEvent`
- Receive ride matching results

**Port:** `8083`

---

### 2. Location Service

Responsible for managing driver locations and performing geospatial searches.

Responsibilities:

- Store driver locations using Redis GEO
- Update driver locations
- Search for nearby drivers
- Return drivers within a specified radius

**Port:** `8082`

Redis GEO is used to efficiently perform location-based searches.

Example:

```text
Passenger:
Latitude:  -1.286389
Longitude: 36.817223

Search radius:
5 km

Result:

Driver A → 0.7 km
Driver B → 1.4 km
Driver C → 2.3 km
```

---

### 3. Matching Service

Responsible for matching a ride request with a nearby driver.

Responsibilities:

- Consume `RideRequestedEvent`
- Request nearby drivers from Location Service
- Select the closest available driver
- Publish `RideMatchedEvent`

**Port:** `8084`

The current matching strategy uses a **nearest-driver greedy selection**.

```text
Nearby Drivers
      │
      ▼
Sort / search by distance
      │
      ▼
Select closest driver
      │
      ▼
RideMatchedEvent
```
##  Technologies

- **Java**
- **Spring Boot**
- **Spring Web**
- **Spring Data Redis**
- **Redis GEO**
- **Spring Cloud OpenFeign**
- **Spring Kafka**
- **Apache Kafka**
- **PostgreSQL**
- **Docker / Docker Compose**
- **Lombok**

## Request Flow

A typical ride request follows this flow:

```text
1. Rider requests a ride
          │
          ▼
2. Ride Service creates ride
          │
          ▼
3. Ride Service publishes RideRequestedEvent
          │
          ▼
4. Kafka
          │
          ▼
5. Matching Service consumes event
          │
          ▼
6. Matching Service calls Location Service
          │
          ▼
7. Location Service searches Redis GEO
          │
          ▼
8. Nearby drivers returned
          │
          ▼
9. Matching Service selects nearest driver
          │
          ▼
10. RideMatchedEvent published
```

##  Communication

The services use two different communication patterns.

### Synchronous communication

Matching Service → Location Service

Spring Cloud OpenFeign is used to call the Location Service:

```java
@FeignClient(
        name = "location-service",
        url = "${location.service.url}"
)
public interface LocationServiceClient {

    @GetMapping("/api/v1/locations/drivers/nearby")
    List<NearByDriverResponse> getNearByDrivers(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("radiusInKm") double radiusInKm
    );
}
```

### Asynchronous communication

Ride Service → Kafka → Matching Service

Example event:

```java
public record RideRequestedEvent(
        String rideId,
        double latitude,
        double longitude
) {}
```

Matching Service then publishes:

```java
public record RideMatchedEvent(
        String rideId,
        String driverId
) {}
```

## Redis GEO

Driver locations are stored using Redis geospatial commands.

Conceptually:

```text
GEOADD drivers <longitude> <latitude> <driverId>
```

A nearby-driver search can then be performed using Redis GEO commands.

For example:

```text
Passenger location
        │
        ▼
Redis GEOSEARCH
        │
        ▼
Drivers within 5 km
```

This allows the system to avoid scanning every driver in the database when looking for nearby drivers.

##  Kafka Topics

Example topics:

| Topic | Producer | Consumer |
|---|---|---|
| `ride-requested` | Ride Service | Matching Service |
| `ride-matched` | Matching Service | Ride Service |

##  Configuration

### Ride Service

```yaml
spring:
  application:
    name: ride-service

server:
  port: 8083
```

### Location Service

```yaml
spring:
  application:
    name: location-service

server:
  port: 8082
```

### Matching Service

```yaml
spring:
  application:
    name: matching-service

  kafka:
    bootstrap-servers: localhost:9092

    consumer:
      group-id: matching-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: 8084

location:
  service:
    url: http://localhost:8082
```

## Running the Project

### Prerequisites

Make sure you have installed:

- Java 21+
- Maven
- Docker
- Docker Compose

### Start infrastructure

Start the required infrastructure:

```bash
docker compose up -d
```

This starts services such as:

```text
PostgreSQL
Redis
Kafka
Zookeeper
```

### Start the microservices

Run each service separately:

```bash
cd ride-service
./mvnw spring-boot:run
```

```bash
cd location-service
./mvnw spring-boot:run
```

```bash
cd matching-service
./mvnw spring-boot:run
```

## Project Goal

The goal of this project is to demonstrate the core backend architecture behind a simplified ride-hailing application.

It focuses on:

- Microservice architecture
- Geospatial driver discovery
- Redis GEO
- Event-driven architecture
- Kafka
- Spring Cloud OpenFeign
- Nearest-driver matching
- Service-to-service communication

## Future Improvements

Possible improvements include:

- Driver availability/status management
- Driver acceptance/rejection
- Ride lifecycle management
- Driver location streaming
- Dynamic search radius
- Multiple-driver matching
- Driver timeout handling
- Retry and dead-letter topics
- Kafka partitioning
- Redis-based driver availability
- Authentication and authorization
- API Gateway
- Service discovery
- Distributed tracing
- Resilience4j circuit breakers
- Docker Compose deployment

## 📄 License

This project is intended as a Spring Boot microservices learning/demo project.
