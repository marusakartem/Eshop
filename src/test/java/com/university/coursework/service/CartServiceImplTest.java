package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.CartDTO;
import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.OrderDTO;
import com.university.coursework.entity.*;
import com.university.coursework.repository.CartItemRepository;
import com.university.coursework.repository.CartRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.service.impl.CartServiceImpl;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderService orderService;

    private CartServiceImpl cartService;
    private UUID cartId, productId, userId, cartItemId;
    private CartEntity cartEntity;
    private ProductEntity productEntity;
    private CartItemEntity cartItemEntity;
    private CartItemDTO cartItemDTO;

    @BeforeEach
    void setUp() {

        Mockito.framework().clearInlineMock(this);

        cartService = new CartServiceImpl(cartRepository, productRepository, userRepository,
                cartItemRepository, orderService);

        userId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartItemId = UUID.randomUUID();

        UserEntity userEntity = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .build();

        BrandEntity brandEntity = BrandEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Brand")
                .build();

        productEntity = ProductEntity.builder()
                .id(productId)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .brand(brandEntity)
                .stockQuantity(10)
                .build();

        cartEntity = CartEntity.builder()
                .id(cartId)
                .user(userEntity)
                .build();

        cartItemEntity = CartItemEntity.builder()
                .id(cartItemId)
                .cart(cartEntity)
                .product(productEntity)
                .quantity(2)
                .price(productEntity.getPrice())
                .build();

        cartItemDTO = CartItemDTO.builder()
                .cartId(cartId)
                .productId(productId)
                .quantity(2)
                .build();
    }

    @Test
    void testFindByUserId() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of(cartItemEntity));

        CartDTO result = cartService.findByUserId(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(1, result.getItems().size());
        verify(cartRepository).findByUserId(userId);
    }

    @Test
    void testAddItemToCart() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItemEntity.class))).thenReturn(cartItemEntity);

        CartItemDTO result = cartService.addItemToCart(cartId, cartItemDTO);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        verify(cartItemRepository).save(any(CartItemEntity.class));
    }

    @Test
    void testUpdateCartItem() {
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItemEntity));
        when(cartItemRepository.save(any(CartItemEntity.class))).thenReturn(cartItemEntity);

        CartItemDTO updatedDTO = CartItemDTO.builder()
                .quantity(5)
                .build();

        CartItemDTO result = cartService.updateCartItem(cartItemId, updatedDTO);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
    }

    @Test
    void testRemoveItemFromCart() {
        when(cartItemRepository.existsById(cartItemId)).thenReturn(true);

        cartService.removeItemFromCart(cartItemId);

        verify(cartItemRepository).deleteById(cartItemId);
    }

    @Test
    void testCheckout() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of(cartItemEntity));
        when(orderService.createOrderFromCart(any(), any(), any()))
                .thenReturn(OrderDTO.builder().id(UUID.randomUUID()).build());

        OrderDTO result = cartService.checkout(cartId, "Test Address");

        assertNotNull(result);
        assertNotNull(result.getId());
    }
}