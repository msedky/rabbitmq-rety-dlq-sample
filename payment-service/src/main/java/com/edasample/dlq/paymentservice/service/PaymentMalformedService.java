package com.edasample.dlq.paymentservice.service;

import com.edasample.dlq.paymentservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.paymentservice.model.event.PaymentRefundedEvent;

public interface PaymentMalformedService {
    PaymentCompletedEvent publishInvalidPaymentCompleted();

    PaymentRefundedEvent publishInvalidPaymentRefunded();
}
