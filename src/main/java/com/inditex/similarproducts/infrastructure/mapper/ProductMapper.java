package com.inditex.similarproducts.infrastructure.mapper;

import com.inditex.similarproducts.domain.model.ProductDetail;
import com.inditex.similarproducts.infrastructure.adapter.in.rest.ProductResponseDto;
import com.inditex.similarproducts.infrastructure.adapter.out.rest.ProductDetailDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponseDto toResponseDto(ProductDetail productDetail);

    ProductDetail toDomain(ProductDetailDto productDetailDto);
}
