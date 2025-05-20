package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.ReviewDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.ReviewEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.exception.ProductNotFoundException;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.ReviewRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    private ReviewServiceImpl reviewService;

    private UUID reviewId;
    private UUID productId;
    private UUID userId;
    private ReviewEntity reviewEntity;
    private ReviewDTO reviewDTO;
    private ProductEntity productEntity;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, productRepository, userRepository);
        reviewId = UUID.randomUUID();
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();

        userEntity = UserEntity.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .role(Role.USER)
                .build();

        productEntity = ProductEntity.builder()
                .id(productId)
                .name("Chanel No 5")
                .description("Classic perfume")
                .price(BigDecimal.valueOf(120.99))
                .stockQuantity(50)
                .createdAt(LocalDateTime.now())
                .build();


        reviewEntity = ReviewEntity.builder()
                .id(reviewId)
                .user(userEntity)
                .product(productEntity)
                .rating(5)
                .comment("Great product!")
                .build();


        reviewDTO = ReviewDTO.builder()
                .userId(userId)
                .productId(productId)
                .rating(5)
                .comment("Great product!")
                .build();
    }

    @Test
    void testGetReviewsByProductId() {
        when(reviewRepository.findByProductId(productId)).thenReturn(List.of(reviewEntity));

        List<ReviewDTO> result = reviewService.getReviewsByProductId(productId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewRepository).findByProductId(productId);
    }

    @Test
    void testCreateReview() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(reviewEntity);

        ReviewDTO result = reviewService.createReview(reviewDTO);

        assertNotNull(result);
        assertEquals("Great product!", result.getComment());
        verify(reviewRepository).save(any(ReviewEntity.class));
    }

    @Test
    void testDeleteReview() {
        when(reviewRepository.existsById(reviewId)).thenReturn(true);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository).deleteById(reviewId);
    }

    @Test
    void testDeleteReviewNotFound() {
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> reviewService.deleteReview(reviewId));
    }
}
