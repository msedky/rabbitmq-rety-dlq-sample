package com.edasample.dlq.paymentservice.service.impl;

import com.edasample.dlq.paymentservice.messaging.PaymentEventPublisher;
import com.edasample.dlq.paymentservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.paymentservice.model.event.PaymentRefundedEvent;
import com.edasample.dlq.paymentservice.service.PaymentMalformedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentMalformedServiceImpl implements PaymentMalformedService {

    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public PaymentCompletedEvent publishInvalidPaymentCompleted() {

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("SomeEmail@SomeDomain.com")
                .amount(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1.0, 1000.0))
                        .setScale(2, RoundingMode.HALF_UP))
                .currency("Invalid-Currency")
                .status("COMPLETED")
                .occurredAt(LocalDateTime.now())
                .build();

        paymentEventPublisher.publishPaymentCompletedEvent(event);
        return event;
    }

    @Override
    public PaymentRefundedEvent publishInvalidPaymentRefunded() {
        PaymentRefundedEvent event = PaymentRefundedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("SomeEmail@SomeDomain.com")
                .amount(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1.0, 1000.0))
                        .setScale(2, RoundingMode.HALF_UP))
                .currency("Invalid-Currency")
                .status("REFUNDED")
                .occurredAt(LocalDateTime.now())
                .build();

        paymentEventPublisher.publishPaymentRefundedEvent(event);
        return event;
    }
}