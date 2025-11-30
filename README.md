# Similar Products Service

Backend service for retrieving similar product details, built with **Spring Boot 2.7** and **Hexagonal Architecture**.

## Overview

This service exposes a REST API that returns the full product details of similar products for a given product ID. It orchestrates calls to existing external APIs to fetch similar product IDs and their details, implementing resilience patterns, caching, and validation for a robust and high-performance solution.

## Architecture

The application follows **Hexagonal Architecture** (Ports & Adapters) to isolate the domain logic from frameworks and external dependencies:

```
src/main/java/com/inditex/similarproducts/
├── domain/                          # Pure Java, no framework dependencies
│   ├── model/
│   │   └── ProductDetail.java       # Domain entity (Immutable, Lombok)
│   └── port/
│       ├── in/
│       │   └── GetSimilarProductsUseCase.java    # Input port
│       └── out/
│           └── ProductRepositoryPort.java        # Output port
├── application/
│   └── service/
│       └── SimilarProductsService.java           # Use case implementation (Caching)
├── infrastructure/
│   ├── adapter/
│   │   ├── in/rest/
│   │   │   ├── SimilarProductsController.java    # REST controller (Validation)
│   │   │   └── ProductResponseDto.java           # API Response DTO
│   │   └── out/rest/
│   │       ├── ProductRestClientAdapter.java     # HTTP client (Resilience)
│   │       └── ProductDetailDto.java             # External API DTO
│   ├── config/
│   │   ├── CacheConfig.java                      # Caching configuration
│   │   └── RestClientConfig.java                 # RestTemplate & Executor config
│   ├── mapper/
│   │   └── ProductMapper.java                    # MapStruct Mapper
│   └── exception/
│       ├── GlobalExceptionHandler.java           # Centralized error handling
│       └── ProductNotFoundException.java
└── SimilarProductsApplication.java               # Main class

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

## Configuration

Configuration is defined in `src/main/resources/application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 5000 | Application port |
| `external.api.base-url` | http://localhost:3001 | External API base URL |
| `external.api.timeout.connect` | 2000ms | Connection timeout |
| `external.api.timeout.read` | 5000ms | Read timeout |

### Caching Configuration

The application uses **Caffeine** for local caching.

- **Cache Name**: `similarProducts`
- **TTL**: 1 hour (`expireAfterWrite`)
- **Configuration**: See `CacheConfig.java`

### Resilience4j Configuration

- **Circuit Breaker**: Opens after 50% failure rate in a sliding window of 10 calls.
- **Retry**: Max 3 attempts with exponential backoff (500ms base, 2x multiplier).
- **Timeout**: 5 seconds per product detail call.

## API Documentation

### Get Similar Products

**Endpoint**: `GET /product/{productId}/similar`

**Parameters**:
- `productId` (Path Variable): The ID of the product (Required, non-blank).

**Responses**:

- **200 OK**: List of similar product details.
  ```json
  [
    {
      "id": "2",
      "name": "Dress",
      "price": 19.99,
      "availability": true
    },
    ...
  ]
  ```

- **400 Bad Request**: Invalid `productId` (e.g., blank).
  ```json
  {
    "status": 400,
    "error": "Bad Request",
    "message": "getSimilarProducts.productId: must not be blank"
  }
  ```

- **404 Not Found**: Product not found.
  ```json
  {
    "status": 404,
    "error": "Not Found",
    "message": "Product not found with ID: 999"
  }
  ```

## Evaluation Criteria Compliance

1.  **Code Clarity and Maintainability**
    - **Hexagonal Architecture**: Clear separation of domain, application, and infrastructure.
    - **MapStruct**: Clean DTO mapping.
    - **Lombok**: Reduced verbosity.
    - **Documentation**: Comprehensive README and Javadoc.

2.  **Performance**
    - **Parallel Execution**: Uses `CompletableFuture` to fetch product details concurrently.
    - **Caching**: Caffeine cache reduces redundant external calls.
    - **Connection Pooling**: Optimized `RestTemplate` configuration.

3.  **Resilience**
    - **Circuit Breaker**: Prevents cascading failures.
    - **Retry**: Handles transient network glitches.
    - **Timeouts**: Prevents indefinite blocking.
    - **Graceful Degradation**: Partial results are returned if some product details fail to load.