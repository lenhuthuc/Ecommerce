package com.trash.ecommerce.service;

import com.trash.ecommerce.dto.InvoiceResponse;
import com.trash.ecommerce.entity.*;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.OrderExistsException;
import com.trash.ecommerce.exception.PaymentException;
import com.trash.ecommerce.mapper.InvoiceMapper;
import com.trash.ecommerce.repository.InvoiceRepository;
import com.trash.ecommerce.repository.OrderRepository;
import com.trash.ecommerce.repository.PaymentMethodRepository;
import com.trash.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class InvoiceServiceImpl implements InvoiceService {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private InvoiceItemService invoiceItemService;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private InvoiceMapper invoiceMapper;
    @Override
    @Transactional
    public InvoiceResponse createInvoice(Long userId, Long orderId, Long paymentMethodId) {
        Invoice invoice = new Invoice();
        invoiceRepository.save(invoice);
        Users users = userRepository.findById(userId)
                .orElseThrow(() -> new FindingUserError("user is not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExistsException("Order not found"));
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new PaymentException("Payment method not found"));
        Set<InvoiceItem> invoiceItems = new HashSet<>();
        BigDecimal totalPrice = BigDecimal.valueOf(0.0);
        for(OrderItem item : order.getOrderItems()) {
            invoiceItems.add(invoiceItemService.makeInvoiceItem(item.getId(), invoice.getId()));
            totalPrice = totalPrice.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        invoice.setOrder(order);
        invoice.setPrice(totalPrice);
        invoice.setCreatedAt(new Date());
        invoice.setUser(users);
        invoice.setPaymentMethod(paymentMethod);
        invoiceRepository.save(invoice);
        return invoiceMapper.MapToDTO(invoice);
    }

    @Override
    public void deleteInvoice(Long userId, Long invoiceId) {

    }
}
