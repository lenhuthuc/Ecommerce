package com.trash.ecommerce.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem implements Serializable {

    @EmbeddedId
    private InvoiceItemId id = new InvoiceItemId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("invoiceId") 
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId") 
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Long quantity = 1L;

   @Column(name = "price",nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    public InvoiceItem() {}

    public InvoiceItem(Invoice invoice, Product product, Long quantity) {
        this.invoice = invoice;
        this.product = product;
        this.quantity = quantity;
        this.id = new InvoiceItemId(invoice.getId(), product.getId());
        this.price = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public InvoiceItemId getId() {
        return id;
    }

    public void setId(InvoiceItemId id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
        if (invoice != null && product != null) {
            this.id = new InvoiceItemId(invoice.getId(), product.getId());
        }
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (invoice != null && product != null) {
            this.id = new InvoiceItemId(invoice.getId(), product.getId());
        }
        if (product != null && this.quantity != null) {
            this.price = product.getPrice().multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
        if (product != null) {
            this.price = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceItem)) return false;
        InvoiceItem that = (InvoiceItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "InvoiceItem{" +
                "invoiceId=" + (id != null ? id.getInvoiceId() : null) +
                ", productId=" + (id != null ? id.getProductId() : null) +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
