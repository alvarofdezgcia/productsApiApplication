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
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.inditex.similarproducts.infrastructure.mapper.ProductMapper;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for similar products endpoint.
 * Inbound adapter that exposes the API on port 5000.
 */
@RestController
@RequestMapping("/product")
@Validated
public class SimilarProductsController {

    private static final Logger logger = LoggerFactory.getLogger(SimilarProductsController.class);

    private final GetSimilarProductsUseCase getSimilarProductsUseCase;
    private final ProductMapper productMapper;

    public SimilarProductsController(GetSimilarProductsUseCase getSimilarProductsUseCase, ProductMapper productMapper) {
        this.getSimilarProductsUseCase = getSimilarProductsUseCase;
        this.productMapper = productMapper;
    }

    /**
     * GET /product/{productId}/similar
     * Returns the list of similar products for a given product ID.
     *
     * @param productId the ID of the product. Must be numeric.
     * @return list of similar product details
     */
    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<ProductResponseDto>> getSimilarProducts(
            @PathVariable @NotBlank(message = "Product ID must not be blank") @Pattern(regexp = "^[0-9]+$", message = "Product ID must be numeric") String productId) {
        logger.info("Received request for similar products of product ID: {}", productId);

        List<ProductDetail> similarProducts = getSimilarProductsUseCase.execute(productId);
        List<ProductResponseDto> response = similarProducts.stream()
                .map(productMapper::toResponseDto)
                .collect(Collectors.toList());

        logger.info("Returning {} similar products for product ID: {}", response.size(), productId);
        return ResponseEntity.ok(response);
    }
}
