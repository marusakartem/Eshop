package com.university.coursework.service.impl;

import com.university.coursework.domain.ReviewDTO;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.entity.ReviewEntity;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.exception.ProductNotFoundException;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.repository.ReviewRepository;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<ReviewDTO> getReviewsByProductId(UUID productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        ProductEntity product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        UserEntity user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new ProductNotFoundException("User not found"));

        ReviewEntity review = ReviewEntity.builder()
                .product(product)
                .user(user)
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .build();

        ReviewEntity savedReview = reviewRepository.save(review);
        return mapToDto(savedReview);
    }

    @Override
    public void deleteReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ProductNotFoundException("Review not found");
        }
        reviewRepository.deleteById(reviewId);
    }

    private ReviewDTO mapToDto(ReviewEntity entity) {
        return ReviewDTO.builder()
                .userId(entity.getUser().getId())
                .productId(entity.getProduct().getId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .build();
    }
}