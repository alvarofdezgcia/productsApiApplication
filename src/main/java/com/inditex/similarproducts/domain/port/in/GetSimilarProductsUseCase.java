package com.inditex.similarproducts.domain.port.in;

import com.inditex.similarproducts.domain.model.ProductDetail;
import java.util.List;

/**
 * Input port (use case) for retrieving similar products.
 * Defines the contract for the application service.
 */
public interface GetSimilarProductsUseCase {
    
    /**
     * Retrieves the list of similar products for a given product ID.
     * 
     * @param productId the ID of the product to find similar products for
     * @return list of similar product details, ordered by similarity
     * @throws ProductNotFoundException if the product does not exist
     */
    List<ProductDetail> execute(String productId);
}
