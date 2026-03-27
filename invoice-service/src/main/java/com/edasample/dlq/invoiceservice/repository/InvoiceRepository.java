package com.edasample.dlq.invoiceservice.repository;

import com.edasample.dlq.invoiceservice.model.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByPaymentId(UUID paymentId);
}