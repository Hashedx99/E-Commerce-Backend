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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewServiceImpl Tests")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = buildUser(UserRole.CUSTOMER);
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("getForProduct: product exists — returns review list")
    void getForProduct_productExists_returnsList() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);
        when(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)).thenReturn(List.of());

        List<ReviewResponse> result = reviewService.getForProduct(productId);

        assertThat(result).isEmpty();
        verify(reviewRepository).findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Test
    @DisplayName("getForProduct: product not found — throws ResourceNotFoundException")
    void getForProduct_productNotFound_throwsException() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getForProduct(productId)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Product");
    }

    @Test
    @DisplayName("create: success — non-verified purchase review")
    void create_success_unverified() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProductIdAndUserId(productId, currentUser.getId())).thenReturn(false);
        when(orderRepository.existsByUserIdAndStatusAndItems_ProductId(currentUser.getId(), OrderStatus.DELIVERED,
                productId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRequest req = new ReviewRequest();
        req.setRating(4);
        req.setTitle("Great product!");
        req.setComment("Really happy with this.");

        ReviewResponse result = reviewService.create(productId, req);

        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.isVerifiedPurchase()).isFalse();
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("create: verified purchase — sets verifiedPurchase flag")
    void create_verifiedPurchase_setsFlag() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProductIdAndUserId(productId, currentUser.getId())).thenReturn(false);
        when(orderRepository.existsByUserIdAndStatusAndItems_ProductId(currentUser.getId(), OrderStatus.DELIVERED,
                productId)).thenReturn(true); // has a delivered order with this product
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRequest req = new ReviewRequest();
        req.setRating(5);

        ReviewResponse result = reviewService.create(productId, req);

        assertThat(result.isVerifiedPurchase()).isTrue();
    }

    @Test
    @DisplayName("create: already reviewed — throws BusinessException")
    void create_alreadyReviewed_throwsException() {
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProductIdAndUserId(productId, currentUser.getId())).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(productId, new ReviewRequest())).isInstanceOf(BusinessException.class).hasMessageContaining("already reviewed");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: success — applies new rating and comment")
    void update_success() {
        UUID reviewId = UUID.randomUUID();
        Review review = buildReview(reviewId, currentUser, 3, "Old title");

        when(reviewRepository.findByIdAndUserId(reviewId, currentUser.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewRequest req = new ReviewRequest();
        req.setRating(5);
        req.setTitle("Updated title");
        req.setComment("Even better than I thought.");

        ReviewResponse result = reviewService.update(reviewId, req);

        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getTitle()).isEqualTo("Updated title");
    }

    @Test
    @DisplayName("update: review belongs to another user — throws ResourceNotFoundException")
    void update_notOwner_throwsException() {
        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findByIdAndUserId(reviewId, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.update(reviewId, new ReviewRequest())).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Review");
    }

    @Test
    @DisplayName("delete: owner deletes own review — soft-deletes successfully")
    void delete_owner_softDeletes() {
        UUID reviewId = UUID.randomUUID();
        Review review = buildReview(reviewId, currentUser, 4, "Good");

        when(reviewRepository.findByIdAndUserId(reviewId, currentUser.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.delete(reviewId);

        assertThat(review.isDeleted()).isTrue();
        verify(reviewRepository).save(review);
    }

    @Test
    @DisplayName("delete: admin deletes any review — soft-deletes successfully")
    void delete_admin_softDeletesAnyReview() {
        // Switch current user to ADMIN
        currentUser.setRole(UserRole.ADMIN);

        UUID reviewId = UUID.randomUUID();
        User otherUser = buildUser(UserRole.CUSTOMER);
        Review review = buildReview(reviewId, otherUser, 2, "Meh");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.delete(reviewId);

        assertThat(review.isDeleted()).isTrue();
        verify(reviewRepository).save(review);
    }

    private User buildUser(UserRole role) {
        User u = new User();
        setId(u, UUID.randomUUID());
        u.setEmail("alice@example.com");
        u.setRole(role);
        return u;
    }

    private Product buildProduct(UUID id) {
        Product p = new Product();
        setId(p, id);
        p.setName("Test Product");
        return p;
    }

    private Review buildReview(UUID id, User user, int rating, String title) {
        Review r = new Review();
        setId(r, id);
        r.setUser(user);
        r.setRating(rating);
        r.setTitle(title);
        r.setComment("Test comment");
        return r;
    }

    private void setId(Object entity, UUID id) {
        try {
            var f = com.hashed.ecombend.common.entity.BaseEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
