package com.inditex.similarproducts.infrastructure.adapter.in.rest;

import com.inditex.similarproducts.domain.model.ProductDetail;
import com.inditex.similarproducts.domain.port.in.GetSimilarProductsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for similar products endpoint.
 * Inbound adapter that exposes the API on port 5000.
 */
@RestController
@RequestMapping("/product")
public class SimilarProductsController {

    private static final Logger logger = LoggerFactory.getLogger(SimilarProductsController.class);

    private final GetSimilarProductsUseCase getSimilarProductsUseCase;

    public SimilarProductsController(GetSimilarProductsUseCase getSimilarProductsUseCase) {
        this.getSimilarProductsUseCase = getSimilarProductsUseCase;
    }

    /**
     * GET /product/{productId}/similar
     * Returns the list of similar products for a given product ID.
     *
     * @param productId the ID of the product
     * @return list of similar product details
     */
    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<ProductDetail>> getSimilarProducts(@PathVariable String productId) {
        logger.info("Received request for similar products of product ID: {}", productId);

        List<ProductDetail> similarProducts = getSimilarProductsUseCase.execute(productId);

        logger.info("Returning {} similar products for product ID: {}", similarProducts.size(), productId);
        return ResponseEntity.ok(similarProducts);
    }
}
