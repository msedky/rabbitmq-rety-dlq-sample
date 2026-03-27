package com.edasample.dlq.invoiceservice.controller;

import com.edasample.dlq.invoiceservice.model.dto.InvoiceResponse;
import com.edasample.dlq.invoiceservice.model.entity.Invoice;
import com.edasample.dlq.invoiceservice.model.enums.InvoiceStatus;
import com.edasample.dlq.invoiceservice.repository.InvoiceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class InvoiceControllerIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("invoices_test_db")
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
    private InvoiceRepository invoiceRepository;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
    }

    @Test
    void getByPaymentId_shouldReturnInvoice_whenExists() throws Exception {

        Invoice invoice = Invoice.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer@test.com")
                .amount(new BigDecimal("180.0"))
                .currency("EGP")
                .status(InvoiceStatus.PAID)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        mockMvc.perform(get("/api/v1/invoices/payment/{paymentId}", savedInvoice.getPaymentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedInvoice.getId().toString()))
                .andExpect(jsonPath("$.paymentId").value(savedInvoice.getPaymentId().toString()))
                .andExpect(jsonPath("$.orderId").value(savedInvoice.getOrderId().toString()))
                .andExpect(jsonPath("$.customerEmail").value(savedInvoice.getCustomerEmail()))
                .andExpect(jsonPath("$.amount").value(savedInvoice.getAmount()))
                .andExpect(jsonPath("$.currency").value(savedInvoice.getCurrency()))
                .andExpect(jsonPath("$.status").value(savedInvoice.getStatus().toString()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.lastUpdatedAt").isNotEmpty());
    }

    @Test
    void getByPaymentId_shouldReturnNotFound_whenDoesNotExists() throws Exception {
        UUID paymentId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/invoices/payment/{paymentId}", paymentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturnAllInvoices() throws Exception {
        Invoice invoice1 = Invoice.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer1@test.com")
                .amount(new BigDecimal("180.0"))
                .currency("EGP")
                .status(InvoiceStatus.PAID)
                .build();
        Invoice invoice2 = Invoice.builder()
                .paymentId(UUID.randomUUID())
                .orderId(invoice1.getOrderId())
                .customerEmail("customer1@test.com")
                .amount(new BigDecimal("180.0"))
                .currency("EGP")
                .status(InvoiceStatus.REFUNDED)
                .build();
        Invoice invoice3 = Invoice.builder()
                .paymentId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .customerEmail("customer2@test.com")
                .amount(new BigDecimal("25.75"))
                .currency("USD")
                .status(InvoiceStatus.PAID)
                .build();

        List<Invoice> savedInvoices = invoiceRepository.saveAll(List.of(invoice1, invoice2, invoice3));

        MvcResult result = mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(savedInvoices.size()))
                .andReturn();

        List<InvoiceResponse> actuals = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<InvoiceResponse>>() {
        });

        for (InvoiceResponse acutal : actuals) {
            Optional<Invoice> optional = savedInvoices.stream().filter(i -> i.getId().equals(acutal.getId())).findAny();
            Assertions.assertTrue(optional.isPresent());
            Invoice invoice = optional.get();
            assertEquals(invoice.getId(), acutal.getId());
            assertEquals(invoice.getPaymentId(), acutal.getPaymentId());
            assertEquals(invoice.getOrderId(), acutal.getOrderId());
            assertEquals(invoice.getCustomerEmail(), acutal.getCustomerEmail());
            assertEquals(0, invoice.getAmount().compareTo(acutal.getAmount()));
            assertEquals(invoice.getCurrency(), acutal.getCurrency());
            assertEquals(invoice.getStatus(), acutal.getStatus());
        }
    }


    @Test
    void getAll_shouldReturnEmptyList_whenNoInvoiceExist() throws Exception {
        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
