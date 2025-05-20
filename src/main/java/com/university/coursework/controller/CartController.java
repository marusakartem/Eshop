package com.university.coursework.controller;

import com.university.coursework.domain.CartDTO;
import com.university.coursework.domain.CartItemDTO;
import com.university.coursework.domain.OrderDTO;
import com.university.coursework.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Carts", description = "APIs for managing user carts")
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get cart by user ID", description = "Retrieves the cart for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<CartDTO> getCartByUserId(@Parameter(description = "User ID") @PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.findByUserId(userId));
    }


    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{cartId}")
    @Operation(summary = "Add item to cart", description = "Adds a new item to the user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<CartItemDTO> addItemToCart(
            @Parameter(description = "Cart ID") @PathVariable UUID cartId,
            @RequestBody CartItemDTO cartItemDTO) {

        return new ResponseEntity<>(cartService.addItemToCart(cartId, cartItemDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Updates the quantity of an item in the cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<CartItemDTO> updateCartItem(
            @Parameter(description = "Cart item ID") @PathVariable UUID itemId,
            @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, cartItemDTO));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the user's cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Void> removeItemFromCart(@Parameter(description = "Cart item ID") @PathVariable UUID itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{cartId}/checkout")
    @Operation(summary = "Checkout cart", description = "Processes the checkout for a user's cart and creates an order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid checkout details")
    })
    public ResponseEntity<OrderDTO> checkout(
            @Parameter(description = "Cart ID") @PathVariable UUID cartId,
            @RequestParam String address) {
        return new ResponseEntity<>(cartService.checkout(cartId, address), HttpStatus.CREATED);
    }
}
