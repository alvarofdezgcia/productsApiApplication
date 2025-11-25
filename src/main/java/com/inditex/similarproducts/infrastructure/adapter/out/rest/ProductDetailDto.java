package com.inditex.similarproducts.infrastructure.adapter.out.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for product detail response from external API.
 */
public class ProductDetailDto {
    
    private String id;
    private String name;
    private Double price;
    private Boolean availability;

    public ProductDetailDto() {
    }

    public ProductDetailDto(String id, String name, Double price, Boolean availability) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.availability = availability;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }
}
