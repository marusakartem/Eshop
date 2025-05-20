package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.ProductDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.BrandRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductManagementIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;


    private UUID userId;
    private UUID productId;
    private UUID brandId;


    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        brandRepository.deleteAll();
        userRepository.deleteAll();
        brandRepository.deleteAll();

        BrandEntity brand = brandRepository.save(BrandEntity.builder()
                .name("Electronics")
                .description("electronics")
                .logoUrl("test-logo")
                .build());
        brandId = brand.getId();

        ProductEntity product = productRepository.save(ProductEntity.builder()
                .name("iPhone 14")
                .description("Latest Apple smartphone with A16 Bionic chip")
                .price(BigDecimal.valueOf(1099))
                .stockQuantity(5)
                .brand(brand)
                .build());
        productId = product.getId();

        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build());
        userId = user.getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldCreateNewProduct() {
        ProductDTO newProduct = ProductDTO.builder()
                .name("MacBook Air M2")
                .description("Lightweight Apple laptop")
                .price(BigDecimal.valueOf(1299))
                .brandId(brandId)
                .stockQuantity(5)
                .build();

        webTestClient.post()
                .uri("/api/v1/products")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductDTO.class)
                .consumeWith(response -> {
                    ProductDTO product = response.getResponseBody();
                    assertNotNull(product);
                    assertEquals("MacBook Air M2", product.getName());
                    assertEquals("Lightweight Apple laptop", product.getDescription());
                    assertEquals(BigDecimal.valueOf(1299), product.getPrice());
                });
    }

    @Test
    void shouldUpdateProductDetails() {
        ProductDTO updatedProduct = ProductDTO.builder()
                .name("iPhone 14 Pro")
                .description("Advanced Apple smartphone")
                .price(BigDecimal.valueOf(1199))
                .brandId(brandId)
                .stockQuantity(6)
                .build();

        webTestClient.put()
                .uri("/api/v1/products/{productId}", productId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updatedProduct)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDTO.class)
                .consumeWith(response -> {
                    ProductDTO product = response.getResponseBody();
                    assertNotNull(product);
                    assertEquals("iPhone 14 Pro", product.getName());
                    assertEquals("Advanced Apple smartphone", product.getDescription());
                    assertEquals(BigDecimal.valueOf(1199), product.getPrice());
                });
    }

    @Test
    void shouldDeleteProduct() {
        webTestClient.delete()
                .uri("/api/v1/products/{productId}", productId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isNoContent();

        assertFalse(productRepository.existsById(productId), "Product should be deleted.");
    }

    @Test
    void shouldReturnNotFoundForUpdatingNonexistentProduct() {
        UUID nonexistentProductId = UUID.randomUUID();

        ProductDTO updatedProduct = ProductDTO.builder()
                .name("iPhone 15 Pro")
                .description("Advanced Apple smartphone")
                .price(BigDecimal.valueOf(1399))
                .brandId(brandId)
                .stockQuantity(6)
                .build();

        webTestClient.put()
                .uri("/api/v1/products/{productId}", nonexistentProductId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(updatedProduct)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found with id: " + nonexistentProductId);
    }

    @Test
    void shouldReturnNotFoundForDeletingNonexistentProduct() {
        UUID nonexistentProductId = UUID.randomUUID();

        webTestClient.delete()
                .uri("/api/v1/products/{productId}", nonexistentProductId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found with id: " + nonexistentProductId);
    }
}