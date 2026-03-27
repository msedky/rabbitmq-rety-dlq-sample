package com.edasample.dlq.invoiceservice.model.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefundedEvent {

    private UUID paymentId;
    private UUID orderId;
    private String customerEmail;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime occurredAt;
}