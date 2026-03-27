package com.edasample.dlq.invoiceservice.config;

public final class RabbitMqConstants {

    private RabbitMqConstants() {
    }

    // Payment completed flow
    public static final String PAYMENT_COMPLETED_EXCHANGE = "payment.completed.ex";
    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.q";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";

    public static final String PAYMENT_COMPLETED_DLX = "payment.completed.dlx";
    public static final String PAYMENT_COMPLETED_DLQ = "payment.completed.dlq";
    public static final String PAYMENT_COMPLETED_DLQ_ROUTING_KEY = "payment.completed.dlq";

    // Payment refunded flow
    public static final String PAYMENT_REFUNDED_EXCHANGE = "payment.refunded.ex";
    public static final String PAYMENT_REFUNDED_QUEUE = "payment.refunded.q";
    public static final String PAYMENT_REFUNDED_ROUTING_KEY = "payment.refunded";


    public static final String PAYMENT_REFUNDED_DLX = "payment.refunded.dlx";
    public static final String PAYMENT_REFUNDED_DLQ = "payment.refunded.dlq";
    public static final String PAYMENT_REFUNDED_DLQ_ROUTING_KEY = "payment.refunded.dlq";
}