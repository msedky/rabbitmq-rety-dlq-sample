package com.edasample.dlq.invoiceservice.model.dto;

import com.edasample.dlq.invoiceservice.model.enums.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private UUID id;
    private UUID paymentId;
    private UUID orderId;
    private String customerEmail;
    private BigDecimal amount;
    private String currency;
    private InvoiceStatus status;
    private Instant createdAt;
    private Instant lastUpdatedAt;
}