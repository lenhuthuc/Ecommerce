package com.trash.ecommerce.repository;

import com.trash.ecommerce.entity.InvoiceItem;
import com.trash.ecommerce.entity.InvoiceItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, InvoiceItemId> {
    List<InvoiceItem> findByInvoiceId(Long invoiceId);
}
