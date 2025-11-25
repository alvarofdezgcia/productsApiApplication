package com.inditex.similarproducts.domain.port.out;

import com.inditex.similarproducts.domain.model.ProductDetail;
import java.util.List;
import java.util.Optional;

/**
 * Output port (repository interface) for accessing product data.
 * Defines the contract for infrastructure adapters.
 */
public interface ProductRepositoryPort {
    
    /**
     * Retrieves the list of similar product IDs for a given product.
     * 
     * @param productId the ID of the product
     * @return list of similar product IDs, ordered by similarity
     */
    List<String> getSimilarProductIds(String productId);
    
    /**
     * Retrieves the product detail for a given product ID.
     * 
     * @param productId the ID of the product
     * @return Optional containing the product detail if found, empty otherwise
     */
    Optional<ProductDetail> getProductDetail(String productId);
}
