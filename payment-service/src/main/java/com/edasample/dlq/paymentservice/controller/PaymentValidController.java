package com.edasample.dlq.paymentservice.controller;

import com.edasample.dlq.paymentservice.model.dto.CreatePaymentRequest;
import com.edasample.dlq.paymentservice.model.dto.PaymentResponse;
import com.edasample.dlq.paymentservice.model.dto.RefundPaymentResponse;
import com.edasample.dlq.paymentservice.service.PaymentValidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/valid/payments")
@RequiredArgsConstructor
public class PaymentValidController {

    private final PaymentValidService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.create(request);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getById(@PathVariable UUID paymentId) {
        return paymentService.getById(paymentId);
    }

    @PostMapping("/{paymentId}/refund")
    public RefundPaymentResponse refund(@PathVariable UUID paymentId) {
        return paymentService.refund(paymentId);
    }

    @GetMapping
    public List<PaymentResponse> getAll(){
        return paymentService.getAll();
    }
}