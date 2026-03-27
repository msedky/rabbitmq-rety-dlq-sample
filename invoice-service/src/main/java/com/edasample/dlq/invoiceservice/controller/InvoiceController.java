package com.edasample.dlq.invoiceservice.controller;

import com.edasample.dlq.invoiceservice.model.dto.InvoiceResponse;
import com.edasample.dlq.invoiceservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/payment/{paymentId}")
    public InvoiceResponse getByPaymentId(@PathVariable UUID paymentId) {
        return invoiceService.getByPaymentId(paymentId);
    }

    @GetMapping
    public List<InvoiceResponse> getAll() {
        return invoiceService.getAll();
    }
}