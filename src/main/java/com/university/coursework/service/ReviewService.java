package com.university.coursework.service;

import com.university.coursework.domain.ReviewDTO;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    List<ReviewDTO> getReviewsByProductId(UUID productId);
    ReviewDTO createReview(ReviewDTO reviewDTO);
    void deleteReview(UUID reviewId);
}