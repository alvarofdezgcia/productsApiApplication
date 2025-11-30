package com.inditex.similarproducts.infrastructure.adapter.out.rest;

import com.inditex.similarproducts.domain.model.ProductDetail;
import com.inditex.similarproducts.domain.port.out.ProductRepositoryPort;
import com.inditex.similarproducts.infrastructure.mapper.ProductMapper;
import com.inditex.similarproducts.infrastructure.exception.ProductNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Adapter for accessing product data from external REST API.
 * Implements the ProductRepositoryPort with resilience patterns.
 */
@Component
public class ProductRestClientAdapter implements ProductRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(ProductRestClientAdapter.class);
    private static final String CIRCUIT_BREAKER_NAME = "productService";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ProductMapper productMapper;

    public ProductRestClientAdapter(
            RestTemplate restTemplate,
            @Value("${external.api.base-url}") String baseUrl,
            ProductMapper productMapper) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.productMapper = productMapper;
    }

    /**
     * Retrieves a list of similar product IDs for a given product.
     * <p>
     * This method calls the external API endpoint: GET
     * /product/{productId}/similarids
     * It is protected by a Circuit Breaker and Retry mechanism.
     * </p>
     *
     * @param productId the ID of the product
     * @return a list of similar product IDs, or an empty list if fallback is
     *         triggered
     * @throws ProductNotFoundException if the product does not exist (404)
     */
    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getSimilarProductIdsFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public List<String> getSimilarProductIds(String productId) {
        String url = baseUrl + "/product/" + productId + "/similarids";
        logger.debug("Fetching similar product IDs from: {}", url);

        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {
                    });

            List<String> similarIds = response.getBody();
            logger.debug("Retrieved {} similar product IDs for product {}",
                    similarIds != null ? similarIds.size() : 0, productId);

            return similarIds != null ? similarIds : List.of();

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Product not found: {}", productId);
            throw new ProductNotFoundException(productId);
        } catch (Exception e) {
            logger.error("Error fetching similar product IDs for {}: {}", productId, e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves the details of a specific product.
     * <p>
     * This method calls the external API endpoint: GET /product/{productId}
     * It is protected by a Circuit Breaker and Retry mechanism.
     * </p>
     *
     * @param productId the ID of the product
     * @return an {@link Optional} containing the product detail, or empty if not
     *         found or fallback triggered
     */
    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductDetailFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public Optional<ProductDetail> getProductDetail(String productId) {
        String url = baseUrl + "/product/" + productId;
        logger.debug("Fetching product detail from: {}", url);

        try {
            ResponseEntity<ProductDetailDto> response = restTemplate.getForEntity(
                    url,
                    ProductDetailDto.class);

            ProductDetailDto dto = response.getBody();
            if (dto == null) {
                logger.warn("Received null response for product {}", productId);
                return Optional.empty();
            }

            ProductDetail productDetail = productMapper.toDomain(dto);

            logger.debug("Successfully retrieved product detail for {}", productId);
            return Optional.of(productDetail);

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Product detail not found for ID: {}", productId);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error fetching product detail for {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fallback method for getSimilarProductIds.
     * Returns empty list when circuit breaker is open or retries are exhausted.
     */
    private List<String> getSimilarProductIdsFallback(String productId, Exception e) {
        logger.error("Fallback triggered for getSimilarProductIds({}): {}", productId, e.getMessage());
        throw new ProductNotFoundException(productId);
    }

    /**
     * Fallback method for getProductDetail.
     * Returns empty Optional when circuit breaker is open or retries are exhausted.
     */
    private Optional<ProductDetail> getProductDetailFallback(String productId, Exception e) {
        logger.warn("Fallback triggered for getProductDetail({}): {}", productId, e.getMessage());
        return Optional.empty();
    }
}
