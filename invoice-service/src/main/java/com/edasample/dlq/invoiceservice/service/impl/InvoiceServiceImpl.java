package com.edasample.dlq.invoiceservice.service.impl;

import com.edasample.dlq.invoiceservice.exception.InvalidCurrencyException;
import com.edasample.dlq.invoiceservice.exception.InvoiceNotFoundException;
import com.edasample.dlq.invoiceservice.exception.NonRetryableInvoiceException;
import com.edasample.dlq.invoiceservice.mapper.InvoiceMapper;
import com.edasample.dlq.invoiceservice.model.dto.InvoiceResponse;
import com.edasample.dlq.invoiceservice.model.entity.Invoice;
import com.edasample.dlq.invoiceservice.model.enums.InvoiceStatus;
import com.edasample.dlq.invoiceservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.invoiceservice.model.event.PaymentRefundedEvent;
import com.edasample.dlq.invoiceservice.repository.InvoiceRepository;
import com.edasample.dlq.invoiceservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private static final String[] VALID_CURRENCIES = {"EGP", "USD", "EUR", "GBP", "SAR", "AED"};

    @Override
    public InvoiceResponse getByPaymentId(UUID paymentId) {
        return invoiceRepository.findByPaymentId(paymentId)
                .map(invoiceMapper::toResponse)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found for paymentId: " + paymentId
                ));
    }

    @Override
    public List<InvoiceResponse> getAll() {
        return invoiceRepository.findAll()
                .stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = InvalidCurrencyException.class,
            noRetryFor = NonRetryableInvoiceException.class,
            maxAttemptsExpression = "${app.retry.invoice.max-attempts}",
            backoff = @Backoff(
                    delayExpression = "${app.retry.invoice.delay}",
                    multiplierExpression = "${app.retry.invoice.multiplier}",
                    maxDelayExpression = "${app.retry.invoice.max-delay}"
            )
    )
    public void processPaymentCompleted(PaymentCompletedEvent event) {
        int attempt = RetrySynchronizationManager.getContext() != null
                ? RetrySynchronizationManager.getContext().getRetryCount() + 1
                : 1;

        log.info("Processing PaymentCompletedEvent for paymentId={}, attempt={}",
                event.getPaymentId(), attempt);

        if (!Arrays.stream(VALID_CURRENCIES).anyMatch(c -> c.equals(event.getCurrency()))) {
            log.warn("Invalid currency '{}' for paymentId={}", event.getCurrency(), event.getPaymentId());
            throw new InvalidCurrencyException(event.getCurrency());
        }

        Invoice invoice = invoiceRepository.findByPaymentId(event.getPaymentId())
                .orElseGet(() -> Invoice.builder()
                        .paymentId(event.getPaymentId())
                        .orderId(event.getOrderId())
                        .customerEmail(event.getCustomerEmail())
                        .amount(event.getAmount())
                        .currency(event.getCurrency())
                        .build());

        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        log.info("Invoice generated successfully for paymentId={}", event.getPaymentId());
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = InvalidCurrencyException.class,
            noRetryFor = NonRetryableInvoiceException.class,
            maxAttemptsExpression = "${app.retry.invoice.max-attempts}",
            backoff = @Backoff(
                    delayExpression = "${app.retry.invoice.delay}",
                    multiplierExpression = "${app.retry.invoice.multiplier}",
                    maxDelayExpression = "${app.retry.invoice.max-delay}"
            )
    )
    public void processPaymentRefunded(PaymentRefundedEvent event) {
        int attempt = RetrySynchronizationManager.getContext() != null
                ? RetrySynchronizationManager.getContext().getRetryCount() + 1
                : 1;

        log.info("Processing PaymentRefundedEvent for paymentId={}, attempt={}",
                event.getPaymentId(), attempt);

        if (!Arrays.stream(VALID_CURRENCIES).anyMatch(c -> c.equals(event.getCurrency()))) {
            throw new InvalidCurrencyException(event.getCurrency());
        }

        Invoice invoice = invoiceRepository.findByPaymentId(event.getPaymentId())
                .orElseThrow(() -> new NonRetryableInvoiceException(
                        "Invoice not found for refund, paymentId=" + event.getPaymentId()
                ));

        invoice.setStatus(InvoiceStatus.REFUNDED);
        invoiceRepository.save(invoice);

        log.info("Invoice marked as REFUNDED for paymentId={}", event.getPaymentId());
    }
}