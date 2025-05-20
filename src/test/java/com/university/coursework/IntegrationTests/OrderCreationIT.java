package com.university.coursework.IntegrationTests;

import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.OrderDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.exception.CartNotFoundException;
import com.university.coursework.repository.*;
import com.university.coursework.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderCreationIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;

    private UUID userId;
    private UUID cartId;
    private UUID productId;

    @BeforeEach
    void setup() {
        orderItemRepository.deleteAll();
        cartItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        brandRepository.deleteAll();
        userRepository.deleteAll();

        BrandEntity brand = brandRepository.findByName("Test brand");
        if (brand == null) {
             brand = BrandEntity.builder()
                    .name("Test brand")
                    .description("test-brand")
                    .logoUrl("test-url")
                    .build();
            brandRepository.saveAndFlush(brand);
        }

        UserEntity user = UserEntity.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userId = userRepository.save(user).getId();

        ProductEntity product = ProductEntity.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .brand(brand)
                .build();
        productId = productRepository.save(product).getId();

        CartEntity cart = CartEntity.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
        cartId = cartRepository.saveAndFlush(cart).getId();

        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
    }


    @Test
    void shouldCreateOrderSuccessfully() {
        assertNotNull(userRepository.findByEmail("test@example.com"), "User not found in DB!");

        webTestClient.post()
                .uri("/api/v1/carts/{cartId}", cartId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(CartItemDTO.builder()
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(2)
                        .price(BigDecimal.valueOf(100))
                        .build()
                )
                .exchange()
                .expectStatus().isCreated();

        OrderDTO response = webTestClient.post()
                .uri("/api/v1/carts/{cartId}/checkout?address=Kyiv", cartId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderDTO.class)
                .returnResult().getResponseBody();

        assertNotNull(response.getId());
        assertEquals("Kyiv", response.getAddress());
        assertEquals(200, response.getTotal().intValue());

        System.out.println("Checking cart after checkout: " + cartRepository.findById(cartId));

        webTestClient.get()
                .uri("/api/v1/carts/{userId}", userId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(0);
    }

    @Test
    @Transactional
    void shouldFailCheckoutWithEmptyCart() {
        cartItemRepository.deleteAll();

        CartEntity savedCart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException("Cart not found with id = " + cartId));
        assertNotNull(savedCart, "Cart should exist before checkout");
        assertTrue(savedCart.getItems() == null || savedCart.getItems().isEmpty(),
                "Cart should be empty before checkout");

        webTestClient.post()
                .uri("/api/v1/carts/{cartId}/checkout?address=Kyiv", cartId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody();
    }
}