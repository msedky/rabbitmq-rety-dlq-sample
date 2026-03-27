package com.edasample.dlq.paymentservice.service.impl;

import com.edasample.dlq.paymentservice.messaging.PaymentEventPublisher;
import com.edasample.dlq.paymentservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.paymentservice.model.event.PaymentRefundedEvent;
import com.edasample.dlq.paymentservice.service.PaymentMalformedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ContextConfiguration(classes = PaymentMalformedServiceImpl.class)
class PaymentMalformedServiceImplTest {

    @MockitoBean
    private PaymentEventPublisher paymentEventPublisher;

    @Autowired
    private PaymentMalformedService paymentMalformedService;

    @Test
    void publishInvalidPaymentCompleted_shouldPublishMalformedEventAndReturnIt() {
        PaymentCompletedEvent event = paymentMalformedService.publishInvalidPaymentCompleted();

        assertNotNull(event);
        assertNotNull(event.getPaymentId());
        assertNotNull(event.getOrderId());
        assertNotNull(event.getCustomerEmail());
        assertNotNull(event.getAmount());
        assertEquals("Invalid-Currency", event.getCurrency());
        assertEquals("COMPLETED", event.getStatus());
        assertNotNull(event.getOccurredAt());

        verify(paymentEventPublisher, times(1)).publishPaymentCompletedEvent(event);
    }

    @Test
    void publishInvalidPaymentRefunded_shouldPublishMalformedEventAndReturnIt() {
        PaymentRefundedEvent event = paymentMalformedService.publishInvalidPaymentRefunded();

        assertNotNull(event);
        assertNotNull(event.getPaymentId());
        assertNotNull(event.getOrderId());
        assertNotNull(event.getCustomerEmail());
        assertNotNull(event.getAmount());
        assertEquals("Invalid-Currency", event.getCurrency());
        assertEquals("REFUNDED", event.getStatus());
        assertNotNull(event.getOccurredAt());

        verify(paymentEventPublisher, times(1)).publishPaymentRefundedEvent(event);
    }
}