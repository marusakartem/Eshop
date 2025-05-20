package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.ProductDTO;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.repository.BrandRepository;
import com.university.coursework.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductDetailsIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    private UUID productId;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        brandRepository.deleteAll();

        BrandEntity brand = brandRepository.save(BrandEntity.builder()
                .name("Electronics")
                .description("electronics")
                .logoUrl("test-logo")
                .build());

        ProductEntity product = productRepository.save(ProductEntity.builder()
                .name("iPhone 13")
                .description("Latest Apple smartphone with A15 Bionic chip")
                .price(BigDecimal.valueOf(999))
                .stockQuantity(5)
                .brand(brand)
                .build());

        productId = product.getId();

    }

    @Test
    void shouldRetrieveProductDetails() {
        webTestClient.get()
                .uri("/api/v1/products/{productId}", productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDTO.class)
                .consumeWith(response -> {
                    ProductDTO product = response.getResponseBody();
                    assertNotNull(product);
                    assertEquals("iPhone 13", product.getName());
                    assertEquals("Latest Apple smartphone with A15 Bionic chip", product.getDescription());
                    assertEquals(0, product.getPrice().compareTo(BigDecimal.valueOf(999)));
                });
    }

    @Test
    void shouldReturnNotFoundForNonexistentProduct() {
        UUID nonexistentProductId = UUID.randomUUID();

        webTestClient.get()
                .uri("/api/v1/products/{productId}", nonexistentProductId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found with id: " + nonexistentProductId);
    }

    @Test
    void shouldReturnBadRequestForInvalidProductId() {
        webTestClient.get()
                .uri("/api/v1/products/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found with id: aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    }
}
