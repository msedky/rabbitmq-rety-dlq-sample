package com.edasample.dlq.invoiceservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMqConfig {

    // ---------------------------
    // Payment Completed Flow
    // ---------------------------

    @Bean
    public TopicExchange paymentCompletedExchange() {
        return new TopicExchange(RabbitMqConstants.PAYMENT_COMPLETED_EXCHANGE);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(RabbitMqConstants.PAYMENT_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.PAYMENT_COMPLETED_DLX)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.PAYMENT_COMPLETED_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding paymentCompletedBinding(TopicExchange paymentCompletedExchange, Queue paymentCompletedQueue) {
        return BindingBuilder.bind(paymentCompletedQueue)
                .to(paymentCompletedExchange)
                .with(RabbitMqConstants.PAYMENT_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public TopicExchange paymentCompletedDlx() {
        return new TopicExchange(RabbitMqConstants.PAYMENT_COMPLETED_DLX);
    }

    @Bean
    public Queue paymentCompletedDlq() {
        return QueueBuilder.durable(RabbitMqConstants.PAYMENT_COMPLETED_DLQ).build();
    }

    @Bean
    public Binding paymentCompletedDlqBinding(TopicExchange paymentCompletedDlx, Queue paymentCompletedDlq) {
        return BindingBuilder.bind(paymentCompletedDlq)
                .to(paymentCompletedDlx)
                .with(RabbitMqConstants.PAYMENT_COMPLETED_DLQ_ROUTING_KEY);
    }

    // ---------------------------
    // Payment Refunded Flow
    // ---------------------------

    @Bean
    public TopicExchange paymentRefundedExchange() {
        return new TopicExchange(RabbitMqConstants.PAYMENT_REFUNDED_EXCHANGE);
    }

    @Bean
    public Queue paymentRefundedQueue() {
        return QueueBuilder.durable(RabbitMqConstants.PAYMENT_REFUNDED_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.PAYMENT_REFUNDED_DLX)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.PAYMENT_REFUNDED_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding paymentRefundedBinding(TopicExchange paymentRefundedExchange, Queue paymentRefundedQueue) {
        return BindingBuilder.bind(paymentRefundedQueue)
                .to(paymentRefundedExchange)
                .with(RabbitMqConstants.PAYMENT_REFUNDED_ROUTING_KEY);
    }

    @Bean
    public TopicExchange paymentRefundedDlx() {
        return new TopicExchange(RabbitMqConstants.PAYMENT_REFUNDED_DLX);
    }

    @Bean
    public Queue paymentRefundedDlq() {
        return QueueBuilder.durable(RabbitMqConstants.PAYMENT_REFUNDED_DLQ).build();
    }

    @Bean
    public Binding paymentRefundedDlqBinding(TopicExchange paymentRefundedDlx, Queue paymentRefundedDlq) {
        return BindingBuilder.bind(paymentRefundedDlq)
                .to(paymentRefundedDlx)
                .with(RabbitMqConstants.PAYMENT_REFUNDED_DLQ_ROUTING_KEY);
    }

    // ---------------------------
    // Message Converter
    // ---------------------------

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}