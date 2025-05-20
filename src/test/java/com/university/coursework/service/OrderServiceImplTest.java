package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.OrderDTO;
import com.university.coursework.entity.*;
import com.university.coursework.exception.OrderNotFoundException;
import com.university.coursework.repository.CartItemRepository;
import com.university.coursework.repository.OrderItemRepository;
import com.university.coursework.repository.OrderRepository;
import com.university.coursework.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    private OrderServiceImpl orderService;

    private UUID orderId;
    private UUID cartId;
    private UUID userId;
    private UUID cartItemId;
    private UUID productId;
    private CartEntity cartEntity;
    private OrderEntity orderEntity;
    private ProductEntity productEntity;
    private CartItemEntity cartItemEntity;
    private OrderItemEntity orderItemEntity;


    @BeforeEach
    void setUp() {
        Mockito.framework().clearInlineMock(this);

        orderService = new OrderServiceImpl(orderRepository, orderItemRepository, cartItemRepository);
        orderId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        userId = UUID.randomUUID();
        cartItemId = UUID.randomUUID();
        productId = UUID.randomUUID();

        productEntity = ProductEntity.builder()
                .id(productId)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .build();

        cartEntity = CartEntity.builder()
                .id(cartId)
                .user(new UserEntity())
                .build();

        cartItemEntity = CartItemEntity.builder()
                .id(cartItemId)
                .cart(cartEntity)
                .product(productEntity)
                .quantity(2)
                .price(productEntity.getPrice())
                .build();

        orderEntity = OrderEntity.builder()
                .id(orderId)
                .user(cartEntity.getUser())
                .total(BigDecimal.valueOf(100))
                .status("CREATED")
                .address("Test Address")
                .items(new ArrayList<>())
                .build();

        orderItemEntity = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .product(productEntity)
                .order(orderEntity)
                .quantity(2)
                .price(productEntity.getPrice())
                .build();
    }

    @Test
    void testCreateOrderFromCart() {
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            return order.toBuilder()
                    .id(orderId)
                    .items(order.getItems().stream()
                            .map(item -> item.toBuilder().id(UUID.randomUUID()).build())
                            .toList())
                    .build();
        });
        when(orderItemRepository.saveAll(any())).thenReturn(List.of(orderItemEntity));

        OrderDTO result = orderService.createOrderFromCart(cartEntity, List.of(cartItemEntity), "Test Address");

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
    }

    @Test
    void testFindAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(orderEntity));

        List<OrderDTO> result = orderService.findAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void testFindOrdersByUserId() {
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(orderEntity));

        List<OrderDTO> result = orderService.findOrdersByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByUserId(userId);
    }

    @Test
    void testFindOrderById() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        OrderDTO result = orderService.findOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void testFindOrderByIdNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.findOrderById(orderId));
    }

    @Test
    void testUpdateOrderStatus() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);

        OrderDTO result = orderService.updateOrderStatus(orderId, "SHIPPED");

        assertNotNull(result);
        assertEquals("SHIPPED", result.getStatus());
        verify(orderRepository).save(any(OrderEntity.class));
    }
}
