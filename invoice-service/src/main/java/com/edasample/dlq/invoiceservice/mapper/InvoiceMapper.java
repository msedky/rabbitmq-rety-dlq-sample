package com.edasample.dlq.invoiceservice.mapper;

import com.edasample.dlq.invoiceservice.model.dto.InvoiceResponse;
import com.edasample.dlq.invoiceservice.model.entity.Invoice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    InvoiceResponse toResponse(Invoice invoice);
}