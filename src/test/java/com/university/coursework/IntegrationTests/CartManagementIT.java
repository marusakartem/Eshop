package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.repository.*;
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


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CartManagementIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    private UUID cartId;
    private UUID productId;
    private UUID userId;

    @BeforeEach
    void setup() {
        orderItemRepository.deleteAll();
        cartItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build());
        userId = user.getId();

        BrandEntity brand = brandRepository.save(BrandEntity.builder()
                .name("Electronics")
                .description("electronics")
                .logoUrl("testUrl")
                .build());

        ProductEntity product = productRepository.save(ProductEntity.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .brand(brand)
                .build());

        productId = product.getId();

        CartEntity cart = cartRepository.save(CartEntity.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .build());

        cartId = cart.getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }

    @Test
    void shouldAddProductToCart() {
        webTestClient.post()
                .uri("/api/v1/carts/{cartId}", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(CartItemDTO.builder()
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(2)
                        .price(BigDecimal.valueOf(100))
                        .build()
                )
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/carts/{userId}", userId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(1)
                .jsonPath("$.items[0].productId").isEqualTo(productId.toString())
                .jsonPath("$.items[0].quantity").isEqualTo(2);
    }

    @Test
    void shouldUpdateProductQuantityInCart() {
        webTestClient.post()
                .uri("/api/v1/carts/{cartId}", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(CartItemDTO.builder()
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(2)
                        .price(BigDecimal.valueOf(100))
                        .build()
                )
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/carts/{cartId}", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(CartItemDTO.builder()
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(2)
                        .price(BigDecimal.valueOf(100))
                        .build()
                )
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/carts/{userId}", userId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].quantity").isEqualTo(4);
    }

    @Test
    void shouldFailToAddProductToNonexistentCart() {
        UUID nonexistentCartId = UUID.randomUUID();

        webTestClient.post()
                .uri("/api/v1/carts/{cartId}", nonexistentCartId)
                .header("Authorization", "Bearer " + userToken).
                contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(CartItemDTO.builder()
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(2)
                        .price(BigDecimal.valueOf(100))
                        .build()
                )
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cart not found with id: " + nonexistentCartId);
    }

    @Test
    void shouldFailToAddNonexistentProductToCart() {
        UUID nonexistentProductId = UUID.randomUUID();

        webTestClient.post()
                .uri("/api/v1/carts/{cartId}", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(CartItemDTO.builder()
                        .cartId(cartId)
                        .productId(nonexistentProductId)
                        .quantity(2)
                        .price(BigDecimal.valueOf(100))
                        .build()
                )
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found with id: " + nonexistentProductId);
    }
}

