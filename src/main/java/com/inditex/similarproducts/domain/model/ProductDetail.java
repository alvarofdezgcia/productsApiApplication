package com.inditex.similarproducts.domain.model;

import lombok.Value;

/**
 * Domain entity representing a product detail.
 * Pure Java class with no framework dependencies.
 * Immutable value object.
 */
@Value
public class ProductDetail {
    String id;
    String name;
    Double price;
    Boolean availability;
}
