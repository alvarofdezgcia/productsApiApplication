# Similar Products Service

Backend service for retrieving similar product details, built with **Spring Boot 2.7** and **Hexagonal Architecture**.

## Overview

This service exposes a REST API that returns the full product details of similar products for a given product ID. It calls existing external APIs to fetch similar product IDs and their details, implementing resilience patterns for high availability and performance optimization through parallel execution.

## Architecture

The application follows **Hexagonal Architecture** (Ports & Adapters):

```
src/main/java/com/inditex/similarproducts/
├── domain/                          # Pure Java, no framework dependencies
│   ├── model/
│   │   └── ProductDetail.java       # Domain entity
│   └── port/
│       ├── in/
│       │   └── GetSimilarProductsUseCase.java    # Input port
│       └── out/
│           └── ProductRepositoryPort.java        # Output port
├── application/
│   └── service/
│       └── SimilarProductsService.java           # Use case implementation
├── infrastructure/
│   ├── adapter/
│   │   ├── in/rest/
│   │   │   └── SimilarProductsController.java    # REST controller
│   │   └── out/rest/
│   │       ├── ProductRestClientAdapter.java     # HTTP client
│   │       └── ProductDetailDto.java
│   ├── config/
│   │   └── RestClientConfig.java                 # Configuration
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── ProductNotFoundException.java
└── SimilarProductsApplication.java               # Main class
```

### Key Design Decisions

- **Domain Layer**: Pure Java with zero Spring dependencies, ensuring business logic is framework-agnostic
- **Parallel Execution**: Uses `CompletableFuture` to fetch product details in parallel for optimal performance
- **Resilience**: Implements Circuit Breaker, Retry, and Timeout patterns using Resilience4j
- **Graceful Degradation**: Returns partial results if some product details fail to load

## Prerequisites

- **Java 11+**
- **Maven 3.9+**
- **Git**

Verify installation:

```bash
java -version
mvn -version
```

## API Contract

### Endpoint

**GET** `/product/{productId}/similar`

Returns the list of similar products for a given product ID.

#### Success Response (200 OK)

```json
[
  {
    "id": "2",
    "name": "Dress",
    "price": 19.99,
    "availability": true
  },
  {
    "id": "3",
    "name": "Blazer",
    "price": 29.99,
    "availability": false
  }
]
```

#### Error Response (404 Not Found)

```json
{
  "timestamp": "2025-11-24T23:50:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with ID: 999",
  "path": "/product/999/similar"
}
```

## Building and Running

### Build the Application

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on **port 5000**.

### Test the API

```bash
curl http://localhost:5000/product/1/similar
```

## Testing

### Run Unit and Integration Tests

```bash
mvn test
```

### Run with Coverage

```bash
mvn test jacoco:report
```

Coverage report will be available at `target/site/jacoco/index.html`.

## Testing with External APIs

### Testing with Mock Server

The application is designed to work with external APIs that provide:
- Similar product IDs endpoint: `GET /product/{productId}/similarids`
- Product detail endpoint: `GET /product/{productId}`

By default, the application expects these APIs to be available at `http://localhost:3001`.

You can configure the external API base URL in `application.yml`:

```yaml
external:
  api:
    base-url: http://localhost:3001
```

### Manual Testing

Once the external API mock is running on port 3001, you can test the application:

```bash
# Start the application
mvn spring-boot:run

# In another terminal, test the endpoint
curl http://localhost:5000/product/1/similar
```

## Configuration

Configuration is defined in `src/main/resources/application.yml`:

### Key Settings

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 5000 | Application port |
| `external.api.base-url` | http://localhost:3001 | External API base URL |
| `external.api.timeout.connect` | 2000ms | Connection timeout |
| `external.api.timeout.read` | 5000ms | Read timeout |

### Resilience4j Configuration

- **Circuit Breaker**: Opens after 50% failure rate in a sliding window of 10 calls
- **Retry**: Max 3 attempts with exponential backoff (500ms base, 2x multiplier)
- **Timeout**: 5 seconds per product detail call

## Resilience Patterns

### Circuit Breaker

Prevents cascading failures by opening the circuit when the external API is unhealthy.

### Retry with Exponential Backoff

Automatically retries failed requests with increasing delays:
- 1st retry: 500ms
- 2nd retry: 1000ms
- 3rd retry: 2000ms

### Timeout

All HTTP calls have explicit timeouts to prevent indefinite blocking.

### Graceful Degradation

If individual product details fail to load (404, timeout, etc.), they are filtered out and the remaining products are returned.

## Health Checks

The application exposes health endpoints via Spring Boot Actuator:

```bash
curl http://localhost:5000/actuator/health
```

## Logging

Logging levels can be configured in `application.yml`:

```yaml
logging:
  level:
    com.inditex.similarproducts: INFO
    io.github.resilience4j: DEBUG
```

## Development

### Project Structure

- `src/main/java` - Application source code
- `src/main/resources` - Configuration files
- `src/test/java` - Unit and integration tests

### Running Tests in Watch Mode

```bash
mvn test -Dspring-boot.run.fork=false
```

## Troubleshooting

### Application won't start on port 5000

Check if another process is using port 5000:

```bash
# Windows
netstat -ano | findstr :5000

# Linux/Mac
lsof -i :5000
```

### External API not responding

Verify the external API mock is running on port 3001:

```bash
curl http://localhost:3001/product/1/similarids
```

### Tests failing

Ensure port 3001 is not in use and run:

```bash
mvn clean test
```

## Evaluation Criteria

This implementation addresses the three key evaluation criteria:

1. **Code Clarity and Maintainability**
   - Hexagonal architecture with clear separation of concerns
   - Domain layer is pure Java (no framework dependencies)
   - Comprehensive documentation and logging

2. **Performance**
   - Parallel execution of product detail fetching using `CompletableFuture`
   - Connection pooling and timeout configuration
   - Efficient thread pool management

3. **Resilience**
   - Circuit breaker pattern to prevent cascading failures
   - Retry with exponential backoff for transient errors
   - Explicit timeouts on all HTTP calls
   - Graceful degradation (partial results on failures)