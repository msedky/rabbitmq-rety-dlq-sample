# RabbitMQ Retry and DLQ Sample

This repository demonstrates a practical **Event-Driven Architecture (EDA)** sample using **Spring Boot microservices**, **RabbitMQ**, **retry**, and **Dead Letter Queue (DLQ)** handling.

The project simulates a payment and invoice flow where one service publishes payment events and another service consumes them asynchronously.  
The main goal of the sample is to show how a system can behave when:

- a valid event is processed successfully
- a malformed event is retried and then sent to DLQ
- a malformed event is considered non-retryable and sent directly to DLQ
- an operator manually fixes the event from RabbitMQ UI and republishes it to the main queue to restore data consistency

---

## Overview

The system consists of two microservices:

- **payment-service**
  - exposes REST APIs for valid payment operations
  - exposes REST APIs for publishing intentionally malformed events
  - stores payment data in **PostgreSQL**
  - publishes:
    - `PaymentCompletedEvent`
    - `PaymentRefundedEvent`

- **invoice-service**
  - consumes payment events from RabbitMQ
  - stores invoice data in **PostgreSQL**
  - applies retry behavior for selected failures
  - routes failed messages to **DLQ**
  - exposes REST APIs for reading invoices

This setup demonstrates how microservices can remain loosely coupled while still handling real-world messaging problems such as retries, poison messages, and recovery from DLQ.

---

## Main Learning Goals

This sample focuses on production-like messaging concerns, especially:

- asynchronous service-to-service communication
- retry with configurable attempts and delay
- Dead Letter Queue handling
- distinguishing between retryable and non-retryable failures
- manual replay of failed messages from RabbitMQ UI
- eventual consistency between services
- practical operational recovery workflow

---

## Architecture Diagram

```mermaid
flowchart LR
    Client[Client / Postman / Curl]

    subgraph PaymentDomain[payment-service]
        PaymentValidAPI[Valid Payment REST API]
        PaymentMalformedAPI[Malformed Payment REST API]
        PaymentService[Service Layer]
        PaymentDB[(PostgreSQL)]
        Publisher[Payment Event Publisher]
    end

    subgraph Broker[RabbitMQ]
        CompletedExchange[payment.completed.ex]
        CompletedQueue[payment.completed.q]
        CompletedDLX[payment.completed.dlx]
        CompletedDLQ[payment.completed.dlq]

        RefundedExchange[payment.refunded.ex]
        RefundedQueue[payment.refunded.q]
        RefundedDLX[payment.refunded.dlx]
        RefundedDLQ[payment.refunded.dlq]
    end

    subgraph InvoiceDomain[invoice-service]
        CompletedListener[PaymentCompletedListener]
        RefundedListener[PaymentRefundedListener]
        InvoiceService[Invoice Service Layer]
        InvoiceDB[(PostgreSQL)]
        InvoiceAPI[Invoice REST API]
    end

    Client --> PaymentValidAPI
    Client --> PaymentMalformedAPI

    PaymentValidAPI --> PaymentService
    PaymentMalformedAPI --> PaymentService
    PaymentService --> PaymentDB
    PaymentService --> Publisher

    Publisher --> CompletedExchange
    Publisher --> RefundedExchange

    CompletedExchange --> CompletedQueue
    RefundedExchange --> RefundedQueue

    CompletedQueue --> CompletedListener
    RefundedQueue --> RefundedListener

    CompletedListener --> InvoiceService
    RefundedListener --> InvoiceService

    InvoiceService --> InvoiceDB
    Client --> InvoiceAPI
    InvoiceAPI --> InvoiceService

    CompletedQueue --> CompletedDLX --> CompletedDLQ
    RefundedQueue --> RefundedDLX --> RefundedDLQ