package com.edasample.dlq.invoiceservice.exception;

public class NonRetryableInvoiceException extends RuntimeException {

    public NonRetryableInvoiceException(String message) {
        super(message);
    }

    public NonRetryableInvoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}