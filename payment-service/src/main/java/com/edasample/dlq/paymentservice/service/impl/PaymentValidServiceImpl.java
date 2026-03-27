package com.edasample.dlq.paymentservice.service.impl;

import com.edasample.dlq.paymentservice.exception.InvalidPaymentStateException;
import com.edasample.dlq.paymentservice.exception.PaymentNotFoundException;
import com.edasample.dlq.paymentservice.mapper.PaymentMapper;
import com.edasample.dlq.paymentservice.messaging.PaymentEventPublisher;
import com.edasample.dlq.paymentservice.model.dto.CreatePaymentRequest;
import com.edasample.dlq.paymentservice.model.dto.PaymentResponse;
import com.edasample.dlq.paymentservice.model.dto.RefundPaymentResponse;
import com.edasample.dlq.paymentservice.model.entity.Payment;
import com.edasample.dlq.paymentservice.model.enums.PaymentStatus;
import com.edasample.dlq.paymentservice.model.event.PaymentCompletedEvent;
import com.edasample.dlq.paymentservice.model.event.PaymentRefundedEvent;
import com.edasample.dlq.paymentservice.repository.PaymentRepository;
import com.edasample.dlq.paymentservice.service.PaymentValidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentValidServiceImpl implements PaymentValidService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public PaymentResponse create(CreatePaymentRequest request) {
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .customerEmail(request.getCustomerEmail())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .customerEmail(savedPayment.getCustomerEmail())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .status(savedPayment.getStatus().name())
                .occurredAt(LocalDateTime.now())
                .build();

        paymentEventPublisher.publishPaymentCompletedEvent(event);

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with id: " + paymentId
                ));

        return paymentMapper.toResponse(payment);
    }

    @Override
    public RefundPaymentResponse refund(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with id: " + paymentId
                ));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException(
                    "Only COMPLETED payments can be refunded. Current status: " + payment.getStatus()
            );
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);

        PaymentRefundedEvent event = PaymentRefundedEvent.builder()
                .paymentId(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .customerEmail(savedPayment.getCustomerEmail())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .status(savedPayment.getStatus().name())
                .occurredAt(LocalDateTime.now())
                .build();

        paymentEventPublisher.publishPaymentRefundedEvent(event);


        return RefundPaymentResponse.builder()
                .id(payment.getId())
                .status(payment.getStatus())
                .message("Payment refunded successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        return paymentMapper.toResponseList(paymentRepository.findAll());
    }
}