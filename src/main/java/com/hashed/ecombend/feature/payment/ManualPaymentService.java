package com.hashed.ecombend.feature.payment;

import com.hashed.ecombend.feature.order.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * This is @Primary so Spring injects it wherever PaymentService is needed.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class ManualPaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Creates a COMPLETED payment record for the given order.
     * No external call is made simulates an instant cash payment.
     *
     * @param order The order to pay
     * @return Persisted Payment with status COMPLETED
     */
    @Override
    public Payment createPayment(Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setProvider(PaymentProvider.MANUAL);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(order.getTotal());
        payment.setCurrency(order.getCurrency());
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        log.info("Manual payment created for order {} — amount: {} {}",
                order.getId(), saved.getAmount(), saved.getCurrency());
        return saved;
    }

    /**
     * Refunds a completed payment by updating its status.
     *
     * @param payment A COMPLETED payment to refund
     * @return Updated payment with status REFUNDED
     */
    @Override
    public Payment refund(Payment payment) {
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment {} refunded for order {}", saved.getId(), saved.getOrder().getId());
        return saved;
    }
}
