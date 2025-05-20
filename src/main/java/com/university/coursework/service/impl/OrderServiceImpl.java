package com.university.coursework.service.impl;

import com.university.coursework.domain.OrderDTO;
import com.university.coursework.domain.OrderItemDTO;
import com.university.coursework.entity.CartEntity;
import com.university.coursework.entity.CartItemEntity;
import com.university.coursework.entity.OrderEntity;
import com.university.coursework.entity.OrderItemEntity;
import com.university.coursework.exception.OrderNotFoundException;
import com.university.coursework.repository.CartItemRepository;
import com.university.coursework.repository.OrderItemRepository;
import com.university.coursework.repository.OrderRepository;
import com.university.coursework.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(CartEntity cart, List<CartItemEntity> cartItems, String address) {
        BigDecimal total = BigDecimal.ZERO;
        OrderEntity order = OrderEntity.builder()
                .user(cart.getUser())
                .address(address)
                .status("CREATED")
                .items(new ArrayList<>())
                .total(total)
                .createdAt(cart.getCreatedAt())
                .build();

        OrderEntity savedOrder = orderRepository.save(order);
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (CartItemEntity cartItem : cartItems) {
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .build();

            orderItems.add(orderItem);
            total = total.add(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        orderItemRepository.saveAll(orderItems);

        savedOrder = order.toBuilder()
                .total(total)
                .items(orderItems)
                .build();
        orderRepository.save(savedOrder);

        cartItemRepository.deleteByCartId(cart.getId());

        return mapToDto(savedOrder);
    }

    @Override
    public List<OrderDTO> findAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> findOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO findOrderById(UUID id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return mapToDto(order);
    }

    @Override
    public OrderDTO updateOrderStatus(UUID id, String status) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (!isValidStatus(status)) {
            throw new RuntimeException("Invalid order status: " + status);
        }

        OrderEntity updatedOrder = order.toBuilder()
                .status(status)
                .build();
        orderRepository.save(updatedOrder);
        return mapToDto(updatedOrder);
    }

    private boolean isValidStatus(String status) {
        return List.of("CREATED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED").contains(status);
    }

    private OrderDTO mapToDto(OrderEntity entity) {
        return OrderDTO.builder()
                .id(entity.getId())
                .total(entity.getTotal())
                .status(entity.getStatus())
                .address(entity.getAddress())
                .items(entity.getItems().stream()
                        .map(this::mapItemToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private OrderItemDTO mapItemToDto(OrderItemEntity entity) {
        return OrderItemDTO.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .productId(entity.getProduct().getId())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .build();
    }
}