package com.edasample.dlq.invoiceservice.service;

import com.edasample.dlq.invoiceservice.model.dto.InvoiceResponse;
import com.edasample.dlq.invoiceservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.invoiceservice.model.event.PaymentRefundedEvent;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    InvoiceResponse getByPaymentId(UUID paymentId);

    List<InvoiceResponse> getAll();

    void processPaymentCompleted(PaymentCompletedEvent event);

    void processPaymentRefunded(PaymentRefundedEvent event);
}