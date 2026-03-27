package com.edasample.dlq.paymentservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange paymentCompletedExchange() {
        return new TopicExchange(RabbitMqConstants.PAYMENT_COMPLETED_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentRefundedExchange() {
        return new TopicExchange(RabbitMqConstants.PAYMENT_REFUNDED_EXCHANGE);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}