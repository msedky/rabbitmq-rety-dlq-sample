package com.edasample.dlq.paymentservice.controller;

import com.edasample.dlq.paymentservice.messaging.PaymentEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentMalformedControllerIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("payments_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentEventPublisher paymentEventPublisher;

    @BeforeEach
    void setUp() {
        doNothing().when(paymentEventPublisher).publishPaymentCompletedEvent(any());
        doNothing().when(paymentEventPublisher).publishPaymentRefundedEvent(any());
    }

    @Test
    void publishInvalidPaymentCompleted_shouldReturnAcceptedAndMalformedEvent() throws Exception {
        mockMvc.perform(post("/api/v1/malformed/payments/payment-completed"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Invalid PaymentCompleted payload published successfully."))
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.event.paymentId").isNotEmpty())
                .andExpect(jsonPath("$.event.orderId").isNotEmpty())
                .andExpect(jsonPath("$.event.customerEmail").value("SomeEmail@SomeDomain.com"))
                .andExpect(jsonPath("$.event.amount").isNumber())
                .andExpect(jsonPath("$.event.currency").value("Invalid-Currency"))
                .andExpect(jsonPath("$.event.status").value("COMPLETED"))
                .andExpect(jsonPath("$.event.occurredAt").isNotEmpty());
    }

    @Test
    void publishInvalidPaymentRefunded_shouldReturnAcceptedAndMalformedEvent() throws Exception {
        mockMvc.perform(post("/api/v1/malformed/payments/payment-refunded"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Invalid PaymentRefunded payload published successfully."))
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.event.paymentId").isNotEmpty())
                .andExpect(jsonPath("$.event.orderId").isNotEmpty())
                .andExpect(jsonPath("$.event.customerEmail").value("SomeEmail@SomeDomain.com"))
                .andExpect(jsonPath("$.event.amount").isNumber())
                .andExpect(jsonPath("$.event.currency").value("Invalid-Currency"))
                .andExpect(jsonPath("$.event.status").value("REFUNDED"))
                .andExpect(jsonPath("$.event.occurredAt").isNotEmpty());
    }
}