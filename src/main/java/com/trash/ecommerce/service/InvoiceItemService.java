package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.InvoiceItemMessageResponse;
import com.trash.ecommerce.dto.InvoiceItemResponse;
import com.trash.ecommerce.entity.InvoiceItem;
import com.trash.ecommerce.entity.InvoiceItemId;

import java.util.List;

public interface InvoiceItemService {
    public InvoiceItem makeInvoiceItem(Long orderItemId, Long invoiceId);
    public InvoiceItemResponse getInvoiceItem(InvoiceItemId invoiceItemId);
    public List<InvoiceItemResponse> getItemsByInvoice(Long invoiceId);
}
