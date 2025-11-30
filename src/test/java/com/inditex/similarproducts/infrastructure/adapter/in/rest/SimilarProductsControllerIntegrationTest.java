package com.inditex.similarproducts.infrastructure.adapter.in.rest;

import com.inditex.similarproducts.domain.model.ProductDetail;
import com.inditex.similarproducts.infrastructure.mapper.ProductMapper;
import com.inditex.similarproducts.infrastructure.adapter.out.rest.ProductDetailDto;
import com.inditex.similarproducts.infrastructure.adapter.in.rest.ProductResponseDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SimilarProductsController.
 * Uses WireMock to mock external API calls.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
                "external.api.base-url=http://localhost:3001"
})
class SimilarProductsControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private CacheManager cacheManager;

        @MockBean
        private ProductMapper productMapper;

        private static WireMockServer wireMockServer;

        @BeforeAll
        static void startServer() {
                wireMockServer = new WireMockServer(3001);
                wireMockServer.start();
                WireMock.configureFor("localhost", 3001);
        }

        @AfterAll
        static void stopServer() {
                wireMockServer.stop();
        }

        @BeforeEach
        void setUp() {
                WireMock.reset();
                if (cacheManager.getCache("similarProducts") != null) {
                        cacheManager.getCache("similarProducts").clear();
                }

                when(productMapper.toDomain(any(ProductDetailDto.class))).thenAnswer(i -> {
                        ProductDetailDto dto = i.getArgument(0);
                        return new ProductDetail(dto.getId(), dto.getName(), dto.getPrice(), dto.getAvailability());
                });

                when(productMapper.toResponseDto(any(ProductDetail.class))).thenAnswer(i -> {
                        ProductDetail detail = i.getArgument(0);
                        return new ProductResponseDto(detail.getId(), detail.getName(), detail.getPrice(),
                                        detail.getAvailability());
                });
        }

        @Test
        void getSimilarProducts_shouldReturnProducts_whenProductExists() throws Exception {
                // Arrange: Mock external API responses
                stubFor(WireMock.get(urlEqualTo("/product/1/similarids"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("[\"2\", \"3\", \"4\"]")));

                stubFor(WireMock.get(urlEqualTo("/product/2"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"2\",\"name\":\"Dress\",\"price\":19.99,\"availability\":true}")));

                stubFor(WireMock.get(urlEqualTo("/product/3"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"3\",\"name\":\"Blazer\",\"price\":29.99,\"availability\":false}")));

                stubFor(WireMock.get(urlEqualTo("/product/4"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"4\",\"name\":\"Boots\",\"price\":39.99,\"availability\":true}")));

                // Act & Assert
                mockMvc.perform(get("/product/1/similar"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$", hasSize(3)))
                                .andExpect(jsonPath("$[*].id", containsInAnyOrder("2", "3", "4")))
                                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Dress", "Blazer", "Boots")))
                                .andExpect(jsonPath("$[*].price", containsInAnyOrder(19.99, 29.99, 39.99)))
                                .andExpect(jsonPath("$[*].availability", containsInAnyOrder(true, false, true)));
        }

        @Test
        void getSimilarProducts_shouldReturnEmptyArray_whenNoSimilarProducts() throws Exception {
                // Arrange
                stubFor(WireMock.get(urlEqualTo("/product/99/similarids"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("[]")));

                // Act & Assert
                mockMvc.perform(get("/product/99/similar"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void getSimilarProducts_shouldReturn404_whenProductNotFound() throws Exception {
                // Arrange
                stubFor(WireMock.get(urlEqualTo("/product/999/similarids"))
                                .willReturn(aResponse()
                                                .withStatus(404)));

                // Act & Assert
                mockMvc.perform(get("/product/999/similar"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("Product not found with ID: 999"));
        }

        @Test
        void getSimilarProducts_shouldFilterOutFailedProducts_whenSomeProductsNotFound() throws Exception {
                // Arrange
                stubFor(WireMock.get(urlEqualTo("/product/1/similarids"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("[\"2\", \"3\", \"4\"]")));

                stubFor(WireMock.get(urlEqualTo("/product/2"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"2\",\"name\":\"Dress\",\"price\":19.99,\"availability\":true}")));

                stubFor(WireMock.get(urlEqualTo("/product/3"))
                                .willReturn(aResponse()
                                                .withStatus(404))); // Product not found

                stubFor(WireMock.get(urlEqualTo("/product/4"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"4\",\"name\":\"Boots\",\"price\":39.99,\"availability\":true}")));

                // Act & Assert
                mockMvc.perform(get("/product/1/similar"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[*].id", containsInAnyOrder("2", "4")));
        }

        @Test
        void getSimilarProducts_shouldReturn400_whenProductIdIsBlank() throws Exception {
                mockMvc.perform(get("/product/ /similar"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getSimilarProducts_shouldReturn400_whenProductIdIsNonNumeric() throws Exception {
                mockMvc.perform(get("/product/abc/similar"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getSimilarProducts_shouldCacheResults_whenCalledTwice() throws Exception {
                // Arrange
                stubFor(WireMock.get(urlEqualTo("/product/1/similarids"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("[\"2\"]")));

                stubFor(WireMock.get(urlEqualTo("/product/2"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withHeader("Content-Type", "application/json")
                                                .withBody("{\"id\":\"2\",\"name\":\"Dress\",\"price\":19.99,\"availability\":true}")));

                // Act
                mockMvc.perform(get("/product/1/similar")).andExpect(status().isOk());
                mockMvc.perform(get("/product/1/similar")).andExpect(status().isOk());

                // Assert
                verify(1, getRequestedFor(urlEqualTo("/product/1/similarids")));
                verify(1, getRequestedFor(urlEqualTo("/product/2")));
        }
}
