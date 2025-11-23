package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.InvoiceResponse;

public interface InvoiceService {
    public InvoiceResponse createInvoice(Long userId, Long orderId, Long paymentMethodId);
    public void deleteInvoice(Long userId, Long invoiceId);
}
