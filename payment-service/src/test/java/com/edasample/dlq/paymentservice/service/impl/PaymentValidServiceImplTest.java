package com.edasample.dlq.paymentservice.service.impl;

import com.edasample.dlq.paymentservice.exception.InvalidPaymentStateException;
import com.edasample.dlq.paymentservice.exception.PaymentNotFoundException;
import com.edasample.dlq.paymentservice.mapper.PaymentMapperImpl;
import com.edasample.dlq.paymentservice.messaging.PaymentEventPublisher;
import com.edasample.dlq.paymentservice.model.dto.CreatePaymentRequest;
import com.edasample.dlq.paymentservice.model.dto.PaymentResponse;
import com.edasample.dlq.paymentservice.model.dto.RefundPaymentResponse;
import com.edasample.dlq.paymentservice.model.entity.Payment;
import com.edasample.dlq.paymentservice.model.enums.PaymentStatus;
import com.edasample.dlq.paymentservice.repository.PaymentRepository;
import com.edasample.dlq.paymentservice.service.PaymentValidService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ContextConfiguration(classes = {
        PaymentValidServiceImpl.class,
        PaymentMapperImpl.class
})
class PaymentValidServiceImplTest {

    @MockitoBean
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentEventPublisher paymentEventPublisher;

    @Autowired
    private PaymentValidService paymentService;

    @Test
    void create_shouldSavePaymentPublishEventAndReturnResponse() {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("180.00"))
                .currency("EGP")
                .build();

        UUID savedId = UUID.randomUUID();

        Payment savedPayment = Payment.builder()
                .id(savedId)
                .orderId(request.getOrderId())
                .customerEmail(request.getCustomerEmail())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.create(request);

        assertNotNull(response);
        assertEquals(savedPayment.getId(), response.getId());
        assertEquals(savedPayment.getOrderId(), response.getOrderId());
        assertEquals(savedPayment.getCustomerEmail(), response.getCustomerEmail());
        assertEquals(0, savedPayment.getAmount().compareTo(response.getAmount()));
        assertEquals(savedPayment.getCurrency(), response.getCurrency());
        assertEquals(savedPayment.getStatus(), response.getStatus());

        verify(paymentRepository, times(1)).save(argThat(payment ->
                payment.getOrderId().equals(request.getOrderId()) &&
                        payment.getCustomerEmail().equals(request.getCustomerEmail()) &&
                        payment.getAmount().compareTo(request.getAmount()) == 0 &&
                        payment.getCurrency().equals(request.getCurrency()) &&
                        payment.getStatus() == PaymentStatus.COMPLETED
        ));

        verify(paymentEventPublisher, times(1)).publishPaymentCompletedEvent(argThat(event ->
                event.getPaymentId().equals(savedPayment.getId()) &&
                        event.getOrderId().equals(savedPayment.getOrderId()) &&
                        event.getCustomerEmail().equals(savedPayment.getCustomerEmail()) &&
                        event.getAmount().compareTo(savedPayment.getAmount()) == 0 &&
                        event.getCurrency().equals(savedPayment.getCurrency()) &&
                        event.getStatus().equals(savedPayment.getStatus().name()) &&
                        event.getOccurredAt() != null
        ));
    }

    @Test
    void getById_shouldReturnPaymentResponse_whenPaymentExists() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("250.00"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getById(paymentId);

        assertNotNull(response);
        assertEquals(payment.getId(), response.getId());
        assertEquals(payment.getOrderId(), response.getOrderId());
        assertEquals(payment.getCustomerEmail(), response.getCustomerEmail());
        assertEquals(0, payment.getAmount().compareTo(response.getAmount()));
        assertEquals(payment.getCurrency(), response.getCurrency());
        assertEquals(payment.getStatus(), response.getStatus());

        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void getById_shouldThrowPaymentNotFoundException_whenPaymentDoesNotExist() {
        UUID paymentId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getById(paymentId));

        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void refund_shouldUpdateStatusPublishEventAndReturnResponse_whenPaymentIsCompleted() {
        UUID paymentId = UUID.randomUUID();

        Payment existingPayment = Payment.builder()
                .id(paymentId)
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("300.00"))
                .currency("EUR")
                .status(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment refundedPayment = Payment.builder()
                .id(existingPayment.getId())
                .orderId(existingPayment.getOrderId())
                .customerEmail(existingPayment.getCustomerEmail())
                .amount(existingPayment.getAmount())
                .currency(existingPayment.getCurrency())
                .status(PaymentStatus.REFUNDED)
                .createdAt(existingPayment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(existingPayment)).thenReturn(refundedPayment);

        RefundPaymentResponse response = paymentService.refund(paymentId);

        assertNotNull(response);
        assertEquals(refundedPayment.getId(), response.getId());
        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
        assertEquals("Payment refunded successfully.", response.getMessage());

        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(existingPayment);
        verify(paymentEventPublisher, times(1)).publishPaymentRefundedEvent(argThat(event ->
                event.getPaymentId().equals(refundedPayment.getId()) &&
                        event.getOrderId().equals(refundedPayment.getOrderId()) &&
                        event.getCustomerEmail().equals(refundedPayment.getCustomerEmail()) &&
                        event.getAmount().compareTo(refundedPayment.getAmount()) == 0 &&
                        event.getCurrency().equals(refundedPayment.getCurrency()) &&
                        event.getStatus().equals(PaymentStatus.REFUNDED.name()) &&
                        event.getOccurredAt() != null
        ));
    }

    @Test
    void refund_shouldThrowPaymentNotFoundException_whenPaymentDoesNotExist() {
        UUID paymentId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.refund(paymentId));

        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentEventPublisher, never()).publishPaymentRefundedEvent(any());
    }

    @Test
    void refund_shouldThrowInvalidPaymentStateException_whenPaymentIsNotCompleted() {
        UUID paymentId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(paymentId)
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("99.00"))
                .currency("USD")
                .status(PaymentStatus.REFUNDED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(InvalidPaymentStateException.class, () -> paymentService.refund(paymentId));

        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentEventPublisher, never()).publishPaymentRefundedEvent(any());
    }

    @Test
    void getAll_shouldReturnAllPayments() {
        Payment payment1 = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer1@test.com")
                .amount(new BigDecimal("100.00"))
                .currency("EGP")
                .status(PaymentStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment payment2 = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer2@test.com")
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .status(PaymentStatus.REFUNDED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findAll()).thenReturn(List.of(payment1, payment2));

        List<PaymentResponse> responses = paymentService.getAll();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(payment1.getId(), responses.get(0).getId());
        assertEquals(payment2.getId(), responses.get(1).getId());

        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoPaymentsExist() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        List<PaymentResponse> responses = paymentService.getAll();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(paymentRepository, times(1)).findAll();
    }
}