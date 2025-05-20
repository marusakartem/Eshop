package com.university.coursework.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class CartItemDTO {
    UUID cartId;
    UUID productId;
    Integer quantity;
    BigDecimal price;
}