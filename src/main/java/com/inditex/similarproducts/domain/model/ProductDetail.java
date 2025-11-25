package com.inditex.similarproducts.domain.model;

import java.util.Objects;

/**
 * Domain entity representing a product detail.
 * Pure Java class with no framework dependencies.
 */
public class ProductDetail {
    
    private final String id;
    private final String name;
    private final Double price;
    private final Boolean availability;

    public ProductDetail(String id, String name, Double price, Boolean availability) {
        this.id = Objects.requireNonNull(id, "Product ID cannot be null");
        this.name = Objects.requireNonNull(name, "Product name cannot be null");
        this.price = Objects.requireNonNull(price, "Product price cannot be null");
        this.availability = Objects.requireNonNull(availability, "Product availability cannot be null");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public Boolean getAvailability() {
        return availability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDetail that = (ProductDetail) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(price, that.price) &&
               Objects.equals(availability, that.availability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, availability);
    }

    @Override
    public String toString() {
        return "ProductDetail{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", price=" + price +
               ", availability=" + availability +
               '}';
    }
}
