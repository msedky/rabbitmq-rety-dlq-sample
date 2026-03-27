package com.edasample.dlq.paymentservice.messaging;

import com.edasample.dlq.paymentservice.config.RabbitMqConstants;
import com.edasample.dlq.paymentservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.paymentservice.model.event.PaymentRefundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompletedEvent for paymentId={}", event.getPaymentId());

        rabbitTemplate.convertAndSend(
                RabbitMqConstants.PAYMENT_COMPLETED_EXCHANGE,
                RabbitMqConstants.PAYMENT_COMPLETED_ROUTING_KEY,
                event
        );

        log.info("PaymentCompletedEvent published successfully for paymentId={}", event.getPaymentId());
    }

    public void publishPaymentRefundedEvent(PaymentRefundedEvent event) {
        log.info("Publishing PaymentRefundedEvent for paymentId={}", event.getPaymentId());

        rabbitTemplate.convertAndSend(
                RabbitMqConstants.PAYMENT_REFUNDED_EXCHANGE,
                RabbitMqConstants.PAYMENT_REFUNDED_ROUTING_KEY,
                event
        );

        log.info("PaymentRefundedEvent published successfully for paymentId={}", event.getPaymentId());
    }
}