package com.university.coursework.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class CartDTO {
    UUID id;
    UUID userId;
    List<CartItemDTO> items;
}