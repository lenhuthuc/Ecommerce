package com.trash.ecommerce.mapper;

import com.trash.ecommerce.dto.InvoiceResponse;
import com.trash.ecommerce.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {
    public InvoiceResponse MapToDTO(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getUser().getId(),
                invoice.getOrder().getId(),
                invoice.getPrice(),
                invoice.getCreatedAt()
        );
    }
}
