# Country Info Service Application

A stateless Spring Boot REST service backed by MySQL. It manages country and language reference data, looks up country information through an external SOAP service, and is packaged to run on Kubernetes.

## Features

- **Country API:** read, update and delete countries with their languages (many-to-many).
- **Validation and error handling:** JSON request validation, consistent JSON error responses (RFC 7807 `ProblemDetail`) .
- **Structured logging:** JSON logs, with every SOAP request, response and fault logged with service, action, outcome and duration.
- **Kubernetes-ready:** multi-stage Docker image, manifests, autoscaling, and deploy scripts for Minikube or any cluster.

## Tech stack

| Area | Technology                                              |
|---|---------------------------------------------------------|
| Language / framework | Java 17, Spring Boot 3.x, Spring Web, Spring Validation |
| Persistence | Spring Data JPA (Hibernate), MySQL 8.4, HikariCP        |
| SOAP client | Spring Web Services (`WebServiceGatewaySupport`), JAXB  |
| Resilience | Resilience4j (retry, circuit breaker, bulkhead)         |
| Caching | Spring Cache with Caffeine                              |
| Observability | Spring Boot Actuator, structured JSON logging           |
| Packaging / runtime | Docker (multi-stage), Kubernetes                        |

## Architecture

```
 client -> [Ingress (optional)] -> Service ncba-app -> pods (2 to 4, stateless)
                                                         |            |
                                                         |            +--> external SOAP service
                                                         |                 (timeout, retry, circuit breaker,
                                                         |                  bulkhead, fallback)
                                                         +--> MySQL (only stateful component)
```

Request flow: `CorrelationIdFilter` assigns an ID, the controller validates the JSON, the service does the work (database or SOAP gateway), and `ApiExceptionHandler` turns failures into JSON errors carrying the same ID.

## API

| Method | Endpoint                      | Description                               | Success |
|--------|-------------------------------|-------------------------------------------|---|
| POST   | `/api/countries/info`         | Full Country Information                  | 200 |
| GET    | `/api/countries/{id}`         | Get one country                           | 200 |
| PUT    | `/api/countries/{id}`         | Replace a country's details and languages | 200 |
| DELETE | `/api/countries/{id}`         | Delete a country                          | 204 |
| GET    | `/actuator/health`            | Health (also `/liveness` and `/readiness`) | 200 |
| GET    | `/actuator/circuitbreakers`   | Circuit breaker state                     | 200 |

**Example: Fetch Full Country Info**

```bash
curl -X POST http://localhost:8080/api/country/info \
  -H "Content-Type: application/json" \
  -d '{"name":"Kenya"}'
```

**Example: SOAP-backed lookup**

```bash
curl -X POST http://localhost:8080/api/customers/lookup \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-123" \
  -d '{"customerId":"1001","reason":"support call"}'
```

**Error responses**

| Code | Meaning                                                   |
|---|-----------------------------------------------------------|
| 400 | Validation failed (field errors in the body)              |
| 404 | Ccountry not found                                        |
| 502 | The SOAP service returned a fault                         |
| 503 + `Retry-After` | Dependency unavailable or overloaded and no fallback data |


## Data model

- `CountryInfo` (id, isoCode, name, capitalCity, phoneCode, continentCode, currencyIsoCode, countryFlag) **many-to-many** `Language` (id, isoCode, name) through the `country_languages` join table.

Entities are never returned directly. The API returns DTO records. Hibernate creates the tables on startup (`ddl-auto=update`), which is for development only.

## Project structure

```
.
├── pom.xml
├── Dockerfile
├── src/main/java/com/ncba/
│   ├── api/         request/response , ApiExceptionHandler
│   ├── configs/     CacheConfig, SoapClient, SoapLoggingInterceptor, SoapClientConfig,
│   ├── controller/  Controllers  
│   ├── dto/         DTOs
│   ├── exception/   exceptions and   ApiExceptionHandler
│   ├── logging/    CorrelationIdFilter
│   ├── model/      JPA entities (Country, Language)
│   ├── repository/ Spring Data repositories
│   ├── service/    business logic (CountryService, CustomerService, ...)
│   └── soap/       CustomerClient, CustomerSoapGateway, SoapClientConfig,
│       │           , generated/ (JAXB classes from the WSDL)
├── k8s.yml            Kubernetes manifests
├── scripts/        deploy and teardown scripts (bash and PowerShell)
├── DEPLOYMENT.md   step-by-step Kubernetes deployment guide
└── DESIGN.md       high-load and failure-handling design
```

## Configuration

Settings are read from `application.properties` and can be overridden by environment variables (Spring maps `SOAP_CO_URL` to `soap.country.info.url`).

| Variable                      | Purpose | Default |
|-------------------------------|---|---|
| `DB_URL`                      | JDBC URL | `jdbc:mysql://localhost:3306/appdb` |
| `DB_USERNAME` / `DB_PASSWORD` | Database login | `appuser` / `apppass` |
| `SOAP_CONTRY_SERVICE_URL`     | SOAP endpoint | set in `application.properties` |
| `SOAP_LOG_PAYLOAD`            | Log SOAP payloads (masked, truncated) | `true` |

In Kubernetes, non-secret values come from a ConfigMap and credentials from a Secret created by the deploy script.

## Run locally

Requirements: Java 17, Maven, Docker.

```bash
# 1. Start MySQL
docker run -d --name mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=rootpass -e MYSQL_DATABASE=appdb \
  -e MYSQL_USER=appuser -e MYSQL_PASSWORD=apppass mysql:8.4

# 2. Run the app
./mvnw spring-boot:run

# 3. Check it
curl http://localhost:8080/actuator/health
```

## Run on Kubernetes

Requirements: Docker, Minikube, kubectl.

```bash
./mvnw clean package
docker build -t ncba-app:1.0.0
kubectl apply -f deployment.yml
kubectl get pods
kubectl get services
```

The script builds the image inside Minikube, creates the namespace, Secret and ConfigMap, deploys MySQL and the app, and applies the autoscaler. To remove everything: `./scripts/teardown.sh` (add `--purge` to delete the database data too).

For other clusters, set `REGISTRY=<your-registry>` so the image is pushed instead of built in Minikube. See [DEPLOYMENT.md](DEPLOYMENT.md) for the full guide and troubleshooting.

## Known limitations (development setup)

- MySQL runs as a single pod without backups. Use a managed database for anything beyond development.
- Default passwords and `ddl-auto=update` are for development. Use real secrets and Flyway or Liquibase otherwise.
- Actuator endpoints are not authenticated.
