package com.edasample.dlq.paymentservice.service;

import com.edasample.dlq.paymentservice.model.dto.CreatePaymentRequest;
import com.edasample.dlq.paymentservice.model.dto.PaymentResponse;
import com.edasample.dlq.paymentservice.model.dto.RefundPaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentValidService {

    PaymentResponse create(CreatePaymentRequest request);

    PaymentResponse getById(UUID paymentId);

    RefundPaymentResponse refund(UUID paymentId);

    List<PaymentResponse> getAll();


}