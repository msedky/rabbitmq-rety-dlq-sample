package com.edasample.dlq.invoiceservice.exception;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(String message) {
        super(message);
    }
}