package com.edasample.dlq.invoiceservice.listener;

import com.edasample.dlq.invoiceservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.invoiceservice.service.InvoiceService;
import org.junit.jupiter.api.Test;
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
@ContextConfiguration(classes = PaymentCompletedListener.class)
class PaymentCompletedListenerTest {

    @MockitoBean
    private InvoiceService invoiceService;

    @Autowired
    private PaymentCompletedListener paymentCompletedListener;

    @Test
    void listen_shouldDelegateToInvoiceService() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("1500.00"))
                .currency("USD")
                .status("COMPLETED")
                .occurredAt(LocalDateTime.now())
                .build();

        paymentCompletedListener.listen(event);

        verify(invoiceService, times(1)).processPaymentCompleted(event);
    }
}