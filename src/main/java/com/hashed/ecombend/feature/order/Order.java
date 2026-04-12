package com.hashed.ecombend.feature.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hashed.ecombend.common.entity.SoftDeleteEntity;
import com.hashed.ecombend.feature.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Total is split into components so we can show itemized receipts,
 * total = subtotal - discountAmount + shippingAmount + taxAmount
 * <p>
 * Shipping address is SNAPSHOTTED onto the order at placement time.
 * If the user later edits or deletes their saved address, this order
 * still records where it was actually sent.
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends SoftDeleteEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "notes")
    private String notes;

    @Column(name = "shipping_name")
    private String shippingName;

    @Column(name = "shipping_line1")
    private String shippingLine1;

    @Column(name = "shipping_line2")
    private String shippingLine2;

    @Column(name = "shipping_city")
    private String shippingCity;

    @Column(name = "shipping_state")
    private String shippingState;

    @Column(name = "shipping_postal_code")
    private String shippingPostalCode;

    @Column(name = "shipping_country")
    private String shippingCountry;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
