package com.edasample.dlq.paymentservice.mapper;

import com.edasample.dlq.paymentservice.model.dto.PaymentResponse;
import com.edasample.dlq.paymentservice.model.entity.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}