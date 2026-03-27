package com.edasample.dlq.invoiceservice.listener;

import com.edasample.dlq.invoiceservice.config.RabbitMqConstants;
import com.edasample.dlq.invoiceservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.invoiceservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedListener {
    private final InvoiceService invoiceService;

    @RabbitListener(queues = RabbitMqConstants.PAYMENT_COMPLETED_QUEUE)
    public void listen(PaymentCompletedEvent event) {
        invoiceService.processPaymentCompleted(event);
    }
}
