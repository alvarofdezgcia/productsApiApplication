package com.inditex.similarproducts.application.service;

import com.inditex.similarproducts.domain.model.ProductDetail;
import com.inditex.similarproducts.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SimilarProductsService.
 */
@ExtendWith(MockitoExtension.class)
class SimilarProductsServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    private SimilarProductsService service;
    private Executor executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(5);
        service = new SimilarProductsService(productRepository, executor);
    }

    @Test
    void execute_shouldReturnSimilarProducts_whenProductsExist() {
        // Arrange
        String productId = "1";
        List<String> similarIds = List.of("2", "3", "4");
        
        ProductDetail product2 = new ProductDetail("2", "Product 2", 29.99, true);
        ProductDetail product3 = new ProductDetail("3", "Product 3", 39.99, false);
        ProductDetail product4 = new ProductDetail("4", "Product 4", 49.99, true);

        when(productRepository.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productRepository.getProductDetail("2")).thenReturn(Optional.of(product2));
        when(productRepository.getProductDetail("3")).thenReturn(Optional.of(product3));
        when(productRepository.getProductDetail("4")).thenReturn(Optional.of(product4));

        // Act
        List<ProductDetail> result = service.execute(productId);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(product2, product3, product4);
        
        verify(productRepository).getSimilarProductIds(productId);
        verify(productRepository).getProductDetail("2");
        verify(productRepository).getProductDetail("3");
        verify(productRepository).getProductDetail("4");
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoSimilarProductsExist() {
        // Arrange
        String productId = "1";
        when(productRepository.getSimilarProductIds(productId)).thenReturn(List.of());

        // Act
        List<ProductDetail> result = service.execute(productId);

        // Assert
        assertThat(result).isEmpty();
        verify(productRepository).getSimilarProductIds(productId);
        verify(productRepository, never()).getProductDetail(anyString());
    }

    @Test
    void execute_shouldFilterOutFailedProducts_whenSomeProductsFail() {
        // Arrange
        String productId = "1";
        List<String> similarIds = List.of("2", "3", "4");
        
        ProductDetail product2 = new ProductDetail("2", "Product 2", 29.99, true);
        ProductDetail product4 = new ProductDetail("4", "Product 4", 49.99, true);

        when(productRepository.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productRepository.getProductDetail("2")).thenReturn(Optional.of(product2));
        when(productRepository.getProductDetail("3")).thenReturn(Optional.empty()); // Failed
        when(productRepository.getProductDetail("4")).thenReturn(Optional.of(product4));

        // Act
        List<ProductDetail> result = service.execute(productId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(product2, product4);
    }

    @Test
    void execute_shouldHandleExceptions_whenRepositoryThrowsException() {
        // Arrange
        String productId = "1";
        List<String> similarIds = List.of("2", "3");
        
        ProductDetail product3 = new ProductDetail("3", "Product 3", 39.99, false);

        when(productRepository.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productRepository.getProductDetail("2")).thenThrow(new RuntimeException("Network error"));
        when(productRepository.getProductDetail("3")).thenReturn(Optional.of(product3));

        // Act
        List<ProductDetail> result = service.execute(productId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).contains(product3);
    }
}
