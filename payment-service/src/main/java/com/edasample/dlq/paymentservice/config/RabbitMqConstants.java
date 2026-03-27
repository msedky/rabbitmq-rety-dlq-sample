package com.edasample.dlq.paymentservice.config;

public final class RabbitMqConstants {

    private RabbitMqConstants() {
    }

    // Payment completed flow
    public static final String PAYMENT_COMPLETED_EXCHANGE = "payment.completed.ex";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";

    // Payment refunded flow
    public static final String PAYMENT_REFUNDED_EXCHANGE = "payment.refunded.ex";
    public static final String PAYMENT_REFUNDED_ROUTING_KEY = "payment.refunded";
}