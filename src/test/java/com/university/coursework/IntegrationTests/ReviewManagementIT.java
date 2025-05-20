package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.ReviewDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.ReviewEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.BrandRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.ReviewRepository;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewManagementIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    private UUID productId;
    private UUID userId;

    @BeforeEach
    void setup() {
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();
        userRepository.deleteAll();
        reviewRepository.deleteAll();


        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.ADMIN)
                .build());

        userId = user.getId();

        BrandEntity brand = brandRepository.save(BrandEntity.builder()
                .name("Electronics")
                .description("electronics")
                .logoUrl("test-logo")
                .build());

        ProductEntity product = productRepository.save(ProductEntity.builder()
                .name("iPhone 14")
                .description("Latest Apple smartphone with A16 Bionic chip")
                .price(BigDecimal.valueOf(1099))
                .stockQuantity(5)
                .brand(brand)
                .build());
        productId = product.getId();

        reviewRepository.save(ReviewEntity.builder()
                .user(user)
                .product(product)
                .rating(5)
                .comment("Great product!")
                .build());

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldCreateReview() {
        ReviewDTO newReview = ReviewDTO.builder()
                .userId(userId)
                .productId(productId)
                .rating(5)
                .comment("Great product!")
                .build();

        webTestClient.post()
                .uri("/api/v1/reviews")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(newReview)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ReviewDTO.class)
                .consumeWith(response -> {
                    ReviewDTO review = response.getResponseBody();
                    assertNotNull(review);
                    assertEquals("Great product!", review.getComment());
                    assertEquals(5, review.getRating());
                });
    }

    @Test
    void shouldGetReviewsByProductId() {
        webTestClient.get()
                .uri("/api/v1/reviews/product/{productId}", productId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReviewDTO.class)
                .consumeWith(response -> {
                    List<ReviewDTO> reviews = response.getResponseBody();
                    assertNotNull(reviews);
                    assertFalse(reviews.isEmpty());
                });
    }

    @Test
    void shouldFailToCreateReviewForNonexistentProduct() {
        ReviewDTO newReview = ReviewDTO.builder()
                .userId(userId)
                .productId(UUID.randomUUID())
                .rating(5)
                .comment("Great product!")
                .build();

        webTestClient.post()
                .uri("/api/v1/reviews")
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(newReview)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found");
    }
}
