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
import com.hashed.ecombend.feature.user.UserRole;
import com.hashed.ecombend.feature.user.address.Address;
import com.hashed.ecombend.feature.user.address.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Product has a @Version field.
 *
 * @Transactional on placeOrder() ensures ALL stock decrements and the order
 * save happen atomically — if anything fails, the entire transaction rolls back.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;

    /**
     * Places an order. Full flow:
     * 1. Validate all products exist and are active.
     * 2. Check stock >= requested quantity for each item.
     * 3. Decrement stock (triggers @Version optimistic lock check on save).
     * 4. Snapshot product name, sku, and price into each OrderItem.
     * 5. Snapshot shipping address if addressId provided.
     * 6. Calculate totals (subtotal, discount stub, shipping stub, tax stub).
     * 7. Save Order + items atomically.
     *
     * @throws InsufficientStockException if stock < quantity (ECB-23)
     * @throws BusinessException          if concurrent stock conflict (retryable)
     * @throws ResourceNotFoundException  if product or address not found
     */
    @Override
    public Order placeOrder(PlaceOrderRequest request) {
        User user = SecurityUtil.getCurrentUser();

        // Step 1: resolve and validate all products up front
        // Load ALL before touching ANY stock — avoid partial failures.
        record ItemData(Product product, int quantity) {
        }
        List<ItemData> itemsData = new ArrayList<>();

        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", itemReq.getProductId()));

            if (!product.isActive()) {
                throw new BusinessException(
                        "Product '" + product.getName() + "' is no longer available");
            }

            // Step 2: stock check
            if (product.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), itemReq.getQuantity(), product.getStock());
            }

            itemsData.add(new ItemData(product, itemReq.getQuantity()));
        }

        // Step 3 & 4: decrement stock + build order items
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(request.getNotes());
        order.setCurrency("USD");

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (ItemData data : itemsData) {
            Product product = data.product();

            // Optimistic lock fires here if another transaction already saved this product
            try {
                product.decrementStock(data.quantity());
                productRepository.save(product);
            } catch (ObjectOptimisticLockingFailureException e) {
                throw new BusinessException(
                        "'" + product.getName()
                                + "' stock changed due to a concurrent request. Please retry.");
            }

            // Snapshot into OrderItem
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setProductSku(product.getSku());
            item.setQuantity(data.quantity());
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(
                    product.getPrice().multiply(BigDecimal.valueOf(data.quantity())));

            orderItems.add(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }

        // Step 5: snapshot shipping address
        if (request.getAddressId() != null) {
            Address address = addressRepository
                    .findByIdAndUserId(request.getAddressId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Address", "id", request.getAddressId()));
            snapshotAddress(order, address);
        }

        // Step 6: calculate totals
        // discount and shipping are stubs
        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingAmount(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setTotal(subtotal);

        // Step 7: save everything
        Order saved = orderRepository.save(order);
        orderItems.forEach(item -> item.setOrder(saved));
