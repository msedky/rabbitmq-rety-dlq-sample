package com.edasample.dlq.paymentservice.controller;

import com.edasample.dlq.paymentservice.service.PaymentMalformedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/malformed/payments")
@RequiredArgsConstructor
public class PaymentMalformedController {

    private final PaymentMalformedService paymentService;

    @PostMapping("/payment-completed")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> publishInvalidPaymentCompleted() {
        return Map.of("message", "Invalid PaymentCompleted payload published successfully.",
                "event", paymentService.publishInvalidPaymentCompleted());
    }

    @PostMapping("/payment-refunded")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> publishInvalidPaymentRefunded() {
        return Map.of("message", "Invalid PaymentRefunded payload published successfully.",
                "event", paymentService.publishInvalidPaymentRefunded());
    }
}