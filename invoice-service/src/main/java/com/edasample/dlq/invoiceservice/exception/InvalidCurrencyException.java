package com.edasample.dlq.invoiceservice.exception;

public class InvalidCurrencyException extends RuntimeException {
    public InvalidCurrencyException(String currency) {
        super("Invalid Currency : " + currency);
    }
}