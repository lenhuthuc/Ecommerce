package com.trash.ecommerce.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class InvoiceItemId implements Serializable {
    private Long invoiceId;
    private Long productId;
    public InvoiceItemId() {}
    public InvoiceItemId(Long invoiceId, Long productId) {
        this.invoiceId = invoiceId;
        this.productId = productId;
    }
    public Long getInvoiceId() {
        return invoiceId;
    }
    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceItemId)) return false;
        InvoiceItemId that = (InvoiceItemId) o;
        return Objects.equals(invoiceId, that.invoiceId) &&
               Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId, productId);
    }
}
