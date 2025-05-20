package com.university.coursework.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class ProductDTO {
    UUID id;
    String name;
    String description;
    UUID brandId;
    BigDecimal price;
    int stockQuantity;
}