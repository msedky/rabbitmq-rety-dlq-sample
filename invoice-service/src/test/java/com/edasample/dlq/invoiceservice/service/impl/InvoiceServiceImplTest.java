package com.edasample.dlq.invoiceservice.service.impl;

import com.edasample.dlq.invoiceservice.exception.InvalidCurrencyException;
import com.edasample.dlq.invoiceservice.exception.InvoiceNotFoundException;
import com.edasample.dlq.invoiceservice.exception.NonRetryableInvoiceException;
import com.edasample.dlq.invoiceservice.mapper.InvoiceMapperImpl;
import com.edasample.dlq.invoiceservice.model.dto.InvoiceResponse;
import com.edasample.dlq.invoiceservice.model.entity.Invoice;
import com.edasample.dlq.invoiceservice.model.enums.InvoiceStatus;
import com.edasample.dlq.invoiceservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.invoiceservice.model.event.PaymentRefundedEvent;
import com.edasample.dlq.invoiceservice.repository.InvoiceRepository;
import com.edasample.dlq.invoiceservice.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ContextConfiguration(classes = {
        InvoiceServiceImpl.class,
        InvoiceMapperImpl.class
})
class InvoiceServiceImplTest {

    @MockitoBean
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Test
    void getByPaymentId_shouldReturnInvoiceResponse_whenInvoiceExists() {
        UUID paymentId = UUID.randomUUID();

        Invoice invoice = Invoice.builder()
                .id(UUID.randomUUID())
                .paymentId(paymentId)
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("1500.00"))
                .currency("USD")
                .status(InvoiceStatus.PAID)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();


        when(invoiceRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(invoice));

        InvoiceResponse invoiceResponse = invoiceService.getByPaymentId(paymentId);

        assertNotNull(invoiceResponse);

        assertEquals(invoice.getId(), invoiceResponse.getId());
        assertEquals(invoice.getPaymentId(), invoiceResponse.getPaymentId());
        assertEquals(invoice.getOrderId(), invoiceResponse.getOrderId());

        assertEquals(invoice.getCustomerEmail(), invoiceResponse.getCustomerEmail());
        assertEquals(invoice.getAmount(), invoiceResponse.getAmount());
        assertEquals(invoice.getCurrency(), invoiceResponse.getCurrency());
        assertEquals(invoice.getStatus(), invoiceResponse.getStatus());

        verify(invoiceRepository, times(1)).findByPaymentId(paymentId);
    }

    @Test
    void getByPaymentId_shouldThrowInvoiceNotFoundException_whenInvoiceDoesNotExist() {
        UUID paymentId = UUID.randomUUID();

        when(invoiceRepository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class,
                () -> invoiceService.getByPaymentId(paymentId));

        verify(invoiceRepository, times(1)).findByPaymentId(paymentId);
    }

    @Test
    void getAll_shouldReturnAllInvoices() {
        Invoice invoice1 = Invoice.builder()
                .id(UUID.randomUUID())
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer1@test.com")
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .status(InvoiceStatus.PAID)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();

        Invoice invoice2 = Invoice.builder()
                .id(UUID.randomUUID())
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer2@test.com")
                .amount(new BigDecimal("2000.00"))
                .currency("EUR")
                .status(InvoiceStatus.REFUNDED)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();

        when(invoiceRepository.findAll()).thenReturn(List.of(invoice1, invoice2));

        List<InvoiceResponse> responses = invoiceService.getAll();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(invoice1.getId(), responses.get(0).getId());
        assertEquals(invoice1.getPaymentId(), responses.get(0).getPaymentId());
        assertEquals(invoice1.getOrderId(), responses.get(0).getOrderId());
        assertEquals(invoice1.getCustomerEmail(), responses.get(0).getCustomerEmail());
        assertEquals(invoice1.getAmount(), responses.get(0).getAmount());
        assertEquals(invoice1.getCurrency(), responses.get(0).getCurrency());
        assertEquals(invoice1.getStatus(), responses.get(0).getStatus());

        assertEquals(invoice2.getId(), responses.get(1).getId());
        assertEquals(invoice2.getPaymentId(), responses.get(1).getPaymentId());
        assertEquals(invoice2.getOrderId(), responses.get(1).getOrderId());
        assertEquals(invoice2.getCustomerEmail(), responses.get(1).getCustomerEmail());
        assertEquals(invoice2.getAmount(), responses.get(1).getAmount());
        assertEquals(invoice2.getCurrency(), responses.get(1).getCurrency());
        assertEquals(invoice2.getStatus(), responses.get(1).getStatus());

        verify(invoiceRepository, times(1)).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoInvoicesExist() {
        when(invoiceRepository.findAll()).thenReturn(List.of());

        List<InvoiceResponse> responses = invoiceService.getAll();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(invoiceRepository, times(1)).findAll();
    }

    @Test
    void processPaymentCompleted_shouldCreateInvoiceAndMarkAsPaid_whenInvoiceDoesNotExist() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("1500.00"))
                .currency("USD")
                .status("COMPLETED")
                .occurredAt(LocalDateTime.now())
                .build();

        when(invoiceRepository.findByPaymentId(event.getPaymentId())).thenReturn(Optional.empty());

        invoiceService.processPaymentCompleted(event);

        verify(invoiceRepository, times(1)).findByPaymentId(event.getPaymentId());
        verify(invoiceRepository, times(1)).save(argThat(invoice ->
                invoice.getPaymentId().equals(event.getPaymentId()) &&
                        invoice.getOrderId().equals(event.getOrderId()) &&
                        invoice.getCustomerEmail().equals(event.getCustomerEmail()) &&
                        invoice.getAmount().compareTo(event.getAmount()) == 0 &&
                        invoice.getCurrency().equals(event.getCurrency()) &&
                        invoice.getStatus() == InvoiceStatus.PAID
        ));
    }

    @Test
    void processPaymentCompleted_shouldUpdateExistingInvoiceAndMarkAsPaid_whenInvoiceAlreadyExists() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("1500.00"))
                .currency("EUR")
                .status("COMPLETED")
                .occurredAt(LocalDateTime.now())
                .build();

        Invoice existingInvoice = Invoice.builder()
                .id(UUID.randomUUID())
                .paymentId(event.getPaymentId())
                .orderId(UUID.randomUUID())
                .customerEmail("old@test.com")
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .status(InvoiceStatus.REFUNDED)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();

        when(invoiceRepository.findByPaymentId(event.getPaymentId())).thenReturn(Optional.of(existingInvoice));

        invoiceService.processPaymentCompleted(event);

        assertEquals(InvoiceStatus.PAID, existingInvoice.getStatus());
        verify(invoiceRepository, times(1)).findByPaymentId(event.getPaymentId());
        verify(invoiceRepository, times(1)).save(existingInvoice);
    }

    @Test
    void processPaymentCompleted_shouldThrowInvalidCurrencyException_whenCurrencyIsInvalid() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("1500.00"))
                .currency("XYZ")
                .status("COMPLETED")
                .occurredAt(LocalDateTime.now())
                .build();

        assertThrows(InvalidCurrencyException.class,
                () -> invoiceService.processPaymentCompleted(event));

        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void processPaymentRefunded_shouldMarkInvoiceAsRefunded_whenInvoiceExistsAndCurrencyIsValid() {
        PaymentRefundedEvent event = PaymentRefundedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("1500.00"))
                .currency("AED")
                .status("REFUNDED")
                .occurredAt(LocalDateTime.now())
                .build();

        Invoice existingInvoice = Invoice.builder()
                .id(UUID.randomUUID())
                .paymentId(event.getPaymentId())
                .orderId(event.getOrderId())
                .customerEmail(event.getCustomerEmail())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .status(InvoiceStatus.PAID)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();

        when(invoiceRepository.findByPaymentId(event.getPaymentId())).thenReturn(Optional.of(existingInvoice));

        invoiceService.processPaymentRefunded(event);

        assertEquals(InvoiceStatus.REFUNDED, existingInvoice.getStatus());
        verify(invoiceRepository, times(1)).findByPaymentId(event.getPaymentId());
        verify(invoiceRepository, times(1)).save(existingInvoice);
    }

    @Test
    void processPaymentRefunded_shouldThrowInvalidCurrencyException_whenCurrencyIsInvalid() {
        PaymentRefundedEvent event = PaymentRefundedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("1500.00"))
                .currency("INVALID")
                .status("REFUNDED")
                .occurredAt(LocalDateTime.now())
                .build();

        assertThrows(InvalidCurrencyException.class,
                () -> invoiceService.processPaymentRefunded(event));

        verify(invoiceRepository, never()).findByPaymentId(any(UUID.class));
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void processPaymentRefunded_shouldThrowNonRetryableInvoiceException_whenInvoiceDoesNotExist() {
        PaymentRefundedEvent event = PaymentRefundedEvent.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("1500.00"))
                .currency("SAR")
                .status("REFUNDED")
                .occurredAt(LocalDateTime.now())
                .build();

        when(invoiceRepository.findByPaymentId(event.getPaymentId())).thenReturn(Optional.empty());

        assertThrows(NonRetryableInvoiceException.class,
                () -> invoiceService.processPaymentRefunded(event));

        verify(invoiceRepository, times(1)).findByPaymentId(event.getPaymentId());
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }
}
