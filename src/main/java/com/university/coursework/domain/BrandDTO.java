package com.university.coursework.domain;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class BrandDTO {
    UUID id;
    String name;
    String description;
    String logoUrl;
}