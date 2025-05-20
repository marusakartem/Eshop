package com.university.coursework.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class ReviewDTO {
    UUID userId;
    UUID productId;
    Integer rating;
    String comment;
}