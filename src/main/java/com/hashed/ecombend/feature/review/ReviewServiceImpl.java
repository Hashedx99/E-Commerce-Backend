package com.hashed.ecombend.feature.review;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.common.util.SecurityUtil;
import com.hashed.ecombend.feature.catalog.product.Product;
import com.hashed.ecombend.feature.catalog.product.ProductRepository;
import com.hashed.ecombend.feature.order.OrderRepository;
import com.hashed.ecombend.feature.order.OrderStatus;
import com.hashed.ecombend.feature.review.dto.ReviewRequest;
import com.hashed.ecombend.feature.review.dto.ReviewResponse;
import com.hashed.ecombend.feature.user.User;
import com.hashed.ecombend.feature.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getForProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    /**
     * Creates a review for a product.
     * One review per user per product — throws if already reviewed.
     * isVerifiedPurchase is set automatically based on delivered orders.
     *
     * @throws BusinessException         if already reviewed this product
     * @throws ResourceNotFoundException if product not found
     */
    @Override
    public ReviewResponse create(UUID productId, ReviewRequest request) {
        User user = SecurityUtil.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new BusinessException(
                    "You have already reviewed this product. Edit your existing review instead.");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());

        // Auto-set verified purchase flag
        review.setVerifiedPurchase(hasDeliveredOrderWithProduct(user.getId(), productId));

        Review saved = reviewRepository.save(review);
        log.info("Review created by {} for product {}", user.getEmail(), product.getName());
        return ReviewResponse.from(saved);
    }

    /**
     * Updates the current user's own review.
