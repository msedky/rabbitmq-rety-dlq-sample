package com.edasample.dlq.paymentservice.messaging;

import com.edasample.dlq.paymentservice.config.RabbitMqConstants;
import com.edasample.dlq.paymentservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.paymentservice.model.event.PaymentRefundedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig
@ContextConfiguration(classes = PaymentEventPublisher.class)
class PaymentEventPublisherTest {

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    @Test
    void publishPaymentCompletedEvent_shouldSendEventToRabbitMq() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("120.00"))
                .currency("USD")
                .status("COMPLETED")
                .occurredAt(LocalDateTime.now())
                .build();

        paymentEventPublisher.publishPaymentCompletedEvent(event);

        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMqConstants.PAYMENT_COMPLETED_EXCHANGE,
                RabbitMqConstants.PAYMENT_COMPLETED_ROUTING_KEY,
                event
        );
    }

    @Test
    void publishPaymentRefundedEvent_shouldSendEventToRabbitMq() {
        PaymentRefundedEvent event = PaymentRefundedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("120.00"))
                .currency("USD")
                .status("REFUNDED")
                .occurredAt(LocalDateTime.now())
                .build();

        paymentEventPublisher.publishPaymentRefundedEvent(event);

        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMqConstants.PAYMENT_REFUNDED_EXCHANGE,
                RabbitMqConstants.PAYMENT_REFUNDED_ROUTING_KEY,
                event
        );
    }
}