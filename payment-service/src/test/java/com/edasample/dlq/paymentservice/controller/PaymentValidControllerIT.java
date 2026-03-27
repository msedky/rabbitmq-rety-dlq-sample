package com.edasample.dlq.paymentservice.controller;

import com.edasample.dlq.paymentservice.messaging.PaymentEventPublisher;
import com.edasample.dlq.paymentservice.model.dto.CreatePaymentRequest;
import com.edasample.dlq.paymentservice.model.dto.PaymentResponse;
import com.edasample.dlq.paymentservice.model.dto.RefundPaymentResponse;
import com.edasample.dlq.paymentservice.model.entity.Payment;
import com.edasample.dlq.paymentservice.model.enums.PaymentStatus;
import com.edasample.dlq.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentValidControllerIT {

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

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentEventPublisher paymentEventPublisher;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        doNothing().when(paymentEventPublisher).publishPaymentCompletedEvent(any());
        doNothing().when(paymentEventPublisher).publishPaymentRefundedEvent(any());
    }

    @Test
    void create_shouldCreatePayment_whenRequestIsValid() throws Exception {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("180.00"))
                .currency("EGP")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/valid/payments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.orderId").value(request.getOrderId().toString()))
                .andExpect(jsonPath("$.customerEmail").value(request.getCustomerEmail()))
                .andExpect(jsonPath("$.amount").value(request.getAmount().doubleValue()))
                .andExpect(jsonPath("$.currency").value(request.getCurrency()))
                .andExpect(jsonPath("$.status").value(PaymentStatus.COMPLETED.toString()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andReturn();

        PaymentResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PaymentResponse.class
        );

        Optional<Payment> optionalPayment = paymentRepository.findById(response.getId());
        Assertions.assertTrue(optionalPayment.isPresent());

        Payment savedPayment = optionalPayment.get();
        assertEquals(response.getId(), savedPayment.getId());
        assertEquals(response.getOrderId(), savedPayment.getOrderId());
        assertEquals(response.getCustomerEmail(), savedPayment.getCustomerEmail());
        assertEquals(0, response.getAmount().compareTo(savedPayment.getAmount()));
        assertEquals(response.getCurrency(), savedPayment.getCurrency());
        assertEquals(response.getStatus(), savedPayment.getStatus());
    }

    @Test
    void getById_shouldReturnPayment_whenExists() throws Exception {
        Payment payment = Payment.builder()
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("300.00"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        mockMvc.perform(get("/api/v1/valid/payments/{paymentId}", savedPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPayment.getId().toString()))
                .andExpect(jsonPath("$.orderId").value(savedPayment.getOrderId().toString()))
                .andExpect(jsonPath("$.customerEmail").value(savedPayment.getCustomerEmail()))
                .andExpect(jsonPath("$.amount").value(savedPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$.currency").value(savedPayment.getCurrency()))
                .andExpect(jsonPath("$.status").value(savedPayment.getStatus().toString()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void getById_shouldReturnNotFound_whenPaymentDoesNotExist() throws Exception {
        UUID paymentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/valid/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void refund_shouldRefundPayment_whenPaymentExistsAndCompleted() throws Exception {
        Payment payment = Payment.builder()
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("250.00"))
                .currency("EUR")
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        MvcResult result = mockMvc.perform(post("/api/v1/valid/payments/{paymentId}/refund", savedPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPayment.getId().toString()))
                .andExpect(jsonPath("$.status").value(PaymentStatus.REFUNDED.toString()))
                .andExpect(jsonPath("$.message").value("Payment refunded successfully."))
                .andReturn();

        RefundPaymentResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RefundPaymentResponse.class
        );

        Optional<Payment> optionalPayment = paymentRepository.findById(response.getId());
        Assertions.assertTrue(optionalPayment.isPresent());
        assertEquals(PaymentStatus.REFUNDED, optionalPayment.get().getStatus());
    }

    @Test
    void refund_shouldReturnNotFound_whenPaymentDoesNotExist() throws Exception {
        UUID paymentId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/valid/payments/{paymentId}/refund", paymentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void refund_shouldReturnBadRequest_whenPaymentIsNotCompleted() throws Exception {
        Payment payment = Payment.builder()
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("250.00"))
                .currency("EUR")
                .status(PaymentStatus.REFUNDED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        mockMvc.perform(post("/api/v1/valid/payments/{paymentId}/refund", savedPayment.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_shouldReturnAllPayments() throws Exception {
        Payment payment1 = Payment.builder()
                .orderId(UUID.randomUUID())
                .customerEmail("customer1@test.com")
                .amount(new BigDecimal("180.00"))
                .currency("EGP")
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment payment2 = Payment.builder()
                .orderId(UUID.randomUUID())
                .customerEmail("customer2@test.com")
                .amount(new BigDecimal("25.00"))
                .currency("USD")
                .status(PaymentStatus.REFUNDED)
                .build();

        List<Payment> savedPayments = paymentRepository.saveAll(List.of(payment1, payment2));

        MvcResult result = mockMvc.perform(get("/api/v1/valid/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(savedPayments.size()))
                .andReturn();

        List<PaymentResponse> actuals = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<PaymentResponse>>() {
                }
        );

        for (PaymentResponse actual : actuals) {
            Optional<Payment> optional = savedPayments.stream()
                    .filter(p -> p.getId().equals(actual.getId()))
                    .findAny();

            Assertions.assertTrue(optional.isPresent());

            Payment payment = optional.get();
            assertEquals(payment.getId(), actual.getId());
            assertEquals(payment.getOrderId(), actual.getOrderId());
            assertEquals(payment.getCustomerEmail(), actual.getCustomerEmail());
            assertEquals(0, payment.getAmount().compareTo(actual.getAmount()));
            assertEquals(payment.getCurrency(), actual.getCurrency());
            assertEquals(payment.getStatus(), actual.getStatus());
        }
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoPaymentsExist() throws Exception {
        mockMvc.perform(get("/api/v1/valid/payments"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}