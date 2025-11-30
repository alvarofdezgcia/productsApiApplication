package com.inditex.similarproducts.infrastructure.mapper;

import com.inditex.similarproducts.domain.model.ProductDetail;
import com.inditex.similarproducts.infrastructure.adapter.in.rest.ProductResponseDto;
import com.inditex.similarproducts.infrastructure.adapter.out.rest.ProductDetailDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toResponseDto_shouldMapCorrectly() {
        ProductDetail domain = new ProductDetail("1", "Name", 10.0, true);
        ProductResponseDto dto = mapper.toResponseDto(domain);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(domain.getId());
        assertThat(dto.getName()).isEqualTo(domain.getName());
        assertThat(dto.getPrice()).isEqualTo(domain.getPrice());
        assertThat(dto.getAvailability()).isEqualTo(domain.getAvailability());
    }

    @Test
    void toDomain_shouldMapCorrectly() {
        ProductDetailDto dto = new ProductDetailDto("1", "Name", 10.0, true);
        ProductDetail domain = mapper.toDomain(dto);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(dto.getId());
        assertThat(domain.getName()).isEqualTo(dto.getName());
        assertThat(domain.getPrice()).isEqualTo(dto.getPrice());
        assertThat(domain.getAvailability()).isEqualTo(dto.getAvailability());
    }
}
