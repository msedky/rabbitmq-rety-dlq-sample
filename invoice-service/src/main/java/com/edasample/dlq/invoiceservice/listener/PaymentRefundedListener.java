package com.edasample.dlq.invoiceservice.listener;

import com.edasample.dlq.invoiceservice.config.RabbitMqConstants;
import com.edasample.dlq.invoiceservice.model.event.PaymentRefundedEvent;
import com.edasample.dlq.invoiceservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRefundedListener {

    private final InvoiceService invoiceService;

    @RabbitListener(queues = RabbitMqConstants.PAYMENT_REFUNDED_QUEUE)
    public void listen(PaymentRefundedEvent event) {
        invoiceService.processPaymentRefunded(event);
    }
}
