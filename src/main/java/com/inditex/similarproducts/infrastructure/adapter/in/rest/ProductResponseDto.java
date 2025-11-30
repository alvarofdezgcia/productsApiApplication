package com.inditex.similarproducts.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private String id;
    private String name;
    private Double price;
    private Boolean availability;
}
