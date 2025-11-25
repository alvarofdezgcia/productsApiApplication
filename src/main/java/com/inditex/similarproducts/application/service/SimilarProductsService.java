package com.inditex.similarproducts.application.service;

import com.inditex.similarproducts.domain.model.ProductDetail;
import com.inditex.similarproducts.domain.port.in.GetSimilarProductsUseCase;
import com.inditex.similarproducts.domain.port.out.ProductRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Application service implementing the use case for retrieving similar products.
 * Orchestrates the domain logic and uses parallel execution for performance.
 */
@Service
public class SimilarProductsService implements GetSimilarProductsUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SimilarProductsService.class);

    private final ProductRepositoryPort productRepository;
    private final Executor taskExecutor;

    public SimilarProductsService(ProductRepositoryPort productRepository, Executor taskExecutor) {
        this.productRepository = productRepository;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public List<ProductDetail> execute(String productId) {
        logger.info("Fetching similar products for product ID: {}", productId);

        // Step 1: Get the list of similar product IDs
        List<String> similarProductIds = productRepository.getSimilarProductIds(productId);
        
        if (similarProductIds.isEmpty()) {
            logger.info("No similar products found for product ID: {}", productId);
            return Collections.emptyList();
        }

        logger.info("Found {} similar product IDs for product {}: {}", 
                    similarProductIds.size(), productId, similarProductIds);

        // Step 2: Fetch product details in parallel
        List<CompletableFuture<Optional<ProductDetail>>> futures = similarProductIds.stream()
                .map(id -> CompletableFuture.supplyAsync(
                        () -> fetchProductDetail(id), 
                        taskExecutor))
                .collect(Collectors.toList());

        // Step 3: Wait for all futures to complete and collect results
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        // Step 4: Filter out empty results and return
        List<ProductDetail> similarProducts = allFutures
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()))
                .join();

        logger.info("Successfully retrieved {} similar product details for product {}", 
                    similarProducts.size(), productId);

        return similarProducts;
    }

    /**
     * Fetches product detail with error handling.
     * Returns empty Optional if the product cannot be retrieved.
     */
    private Optional<ProductDetail> fetchProductDetail(String productId) {
        try {
            return productRepository.getProductDetail(productId);
        } catch (Exception e) {
            logger.warn("Failed to fetch product detail for ID {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }
}
