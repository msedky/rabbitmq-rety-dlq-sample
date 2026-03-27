package com.edasample.dlq.paymentservice.model.dto;

import com.edasample.dlq.paymentservice.model.enums.PaymentStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPaymentResponse {

    private UUID id;
    private PaymentStatus status;
    private String message;
}