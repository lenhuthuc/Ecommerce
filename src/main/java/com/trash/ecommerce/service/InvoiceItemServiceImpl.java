package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.InvoiceItemResponse;
import com.trash.ecommerce.entity.*;
import com.trash.ecommerce.exception.InvoiceException;
import com.trash.ecommerce.exception.OrderExistsException;
import com.trash.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InvoiceItemServiceImpl implements InvoiceItemService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceItemRepository invoiceItemRepository;
    @Override
    public InvoiceItem makeInvoiceItem(Long orderItemId, Long invoiceId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderExistsException("Order item not found"));
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceException("Invoice not found"));
        
        if (orderItem.getProduct() == null) {
            throw new OrderExistsException("Product not found in order item");
        }
        
        InvoiceItem invoiceItem = new InvoiceItem();
        InvoiceItemId id = new InvoiceItemId(invoiceId, orderItem.getProduct().getId());
        invoiceItem.setId(id);
        invoiceItem.setInvoice(invoice);
        invoiceItem.setProduct(orderItem.getProduct()); // Gán Product cho InvoiceItem
        invoiceItem.setPrice(orderItem.getPrice());
        invoiceItem.setQuantity(orderItem.getQuantity());
        invoiceItem.setTotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return invoiceItem;
    }

    @Override
    public InvoiceItemResponse getInvoiceItem(InvoiceItemId invoiceItemId) {
        InvoiceItem invoiceItem = invoiceItemRepository.findById(invoiceItemId)
                .orElseThrow(() -> new RuntimeException("InvoiceItem not found"));

        return new InvoiceItemResponse(
                invoiceItem.getId().getInvoiceId(),
                invoiceItem.getId().getProductId(),
                invoiceItem.getQuantity(),
                invoiceItem.getPrice(),
                invoiceItem.getTotal()
        );
    }

    @Override
    public List<InvoiceItemResponse> getItemsByInvoice(Long invoiceId) {
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoiceId);

        return items.stream()
                .map(item -> new InvoiceItemResponse(
                        item.getId().getInvoiceId(),
                        item.getId().getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getTotal()
                ))
                .toList();
    }

}
