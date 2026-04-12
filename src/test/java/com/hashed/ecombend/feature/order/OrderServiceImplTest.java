package com.hashed.ecombend.feature.order;

import com.hashed.ecombend.common.exception.BusinessException;
import com.hashed.ecombend.common.exception.InsufficientStockException;
import com.hashed.ecombend.common.exception.ResourceNotFoundException;
import com.hashed.ecombend.common.util.SecurityUtil;
import com.hashed.ecombend.feature.catalog.product.Product;
import com.hashed.ecombend.feature.catalog.product.ProductRepository;
import com.hashed.ecombend.feature.order.dto.PlaceOrderRequest;
import com.hashed.ecombend.feature.order.dto.UpdateOrderStatusRequest;
import com.hashed.ecombend.feature.user.User;
import com.hashed.ecombend.feature.user.address.AddressRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository itemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setEmail("alice@example.com");
        setId(currentUser, UUID.randomUUID());

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("placeOrder: success — decrements stock, builds OrderItems, calculates total")
    void placeOrder_success() {
        Product product = buildProduct("Apple Watch", "ELEC-AW-001", new BigDecimal("399.99"), 10);

        PlaceOrderRequest req = new PlaceOrderRequest();
        PlaceOrderRequest.OrderItemRequest itemReq = new PlaceOrderRequest.OrderItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantity(2);
        req.setItems(List.of(itemReq));

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Order savedOrder = new Order();
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setTotal(new BigDecimal("799.98"));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(itemRepository.saveAll(any())).thenReturn(List.of());

        Order result = orderService.placeOrder(req);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        // Stock should have been decremented
        assertThat(product.getStock()).isEqualTo(8);
        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("placeOrder: insufficient stock — throws InsufficientStockException")
    void placeOrder_insufficientStock_throwsException() {
        Product product = buildProduct("Watch", "SKU-001", new BigDecimal("99.99"), 1);

        PlaceOrderRequest req = buildRequest(product.getId(), 5); // wants 5, only 1 in stock

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(req)).isInstanceOf(InsufficientStockException.class).hasMessageContaining("Watch").hasMessageContaining("requested 5").hasMessageContaining("only 1 available");

        // Stock must NOT be touched if check fails
        assertThat(product.getStock()).isEqualTo(1);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("placeOrder: inactive product — throws BusinessException")
    void placeOrder_inactiveProduct_throwsException() {
        Product product = buildProduct("Watch", "SKU-001", new BigDecimal("99.99"), 10);
        product.setActive(false);

        PlaceOrderRequest req = buildRequest(product.getId(), 1);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(req)).isInstanceOf(BusinessException.class).hasMessageContaining("no longer available");
    }

    @Test
    @DisplayName("placeOrder: product not found — throws ResourceNotFoundException")
    void placeOrder_productNotFound_throwsException() {
        UUID missingId = UUID.randomUUID();
        PlaceOrderRequest req = buildRequest(missingId, 1);

        when(productRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(req)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Product");
    }

    @Test
    @DisplayName("getOrder: not found — throws ResourceNotFoundException")
    void getOrder_notFound_throwsException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndUserId(orderId, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(orderId)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Order");
    }

    @Test
    @DisplayName("updateStatus: PENDING → CONFIRMED — succeeds")
    void updateStatus_pendingToConfirmed_succeeds() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateStatus(orderId, req);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("updateStatus: PENDING → REFUNDED — invalid transition throws BusinessException")
    void updateStatus_invalidTransition_throwsException() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.REFUNDED); // illegal from PENDING

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(orderId, req)).isInstanceOf(BusinessException.class).hasMessageContaining("Cannot transition");
    }

    @Test
    @DisplayName("updateStatus: DELIVERED → REFUNDED — valid transition succeeds")
    void updateStatus_deliveredToRefunded_succeeds() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED);

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.REFUNDED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateStatus(orderId, req);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    private Product buildProduct(String name, String sku, BigDecimal price, int stock) {
        Product p = new Product();
        setId(p, UUID.randomUUID());
        p.setName(name);
        p.setSku(sku);
        p.setPrice(price);
        p.setStock(stock);
        p.setActive(true);
        return p;
    }

    private PlaceOrderRequest buildRequest(UUID productId, int quantity) {
        PlaceOrderRequest req = new PlaceOrderRequest();
        PlaceOrderRequest.OrderItemRequest item = new PlaceOrderRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        req.setItems(List.of(item));
        return req;
    }

    /**
     * Sets the UUID id on a BaseEntity via reflection JPA normally does this.
     */
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
