package com.inditex.similarproducts.infrastructure.exception;

/**
 * Exception thrown when a product is not found.
 */
public class ProductNotFoundException extends RuntimeException {
    
    private final String productId;

    public ProductNotFoundException(String productId) {
        super(String.format("Product not found with ID: %s", productId));
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
