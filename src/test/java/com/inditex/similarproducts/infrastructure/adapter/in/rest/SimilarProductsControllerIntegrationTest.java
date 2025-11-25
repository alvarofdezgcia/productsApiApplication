package com.inditex.similarproducts.infrastructure.adapter.in.rest;

import com.inditex.similarproducts.domain.model.ProductDetail;
import com.inditex.similarproducts.infrastructure.exception.ProductNotFoundException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;
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

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(3001);
        wireMockServer.start();
        WireMock.configureFor("localhost", 3001);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
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
}
