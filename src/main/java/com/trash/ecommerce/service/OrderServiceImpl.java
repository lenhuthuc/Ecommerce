package com.trash.ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import com.trash.ecommerce.entity.*;
import com.trash.ecommerce.exception.*;
import com.trash.ecommerce.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.dto.OrderMessageResponseDTO;
import com.trash.ecommerce.dto.OrderResponseDTO;
import com.trash.ecommerce.repository.OrderItemRepository;
import com.trash.ecommerce.repository.OrderRepository;
import com.trash.ecommerce.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final OrderItemRepository orderItemRepository;
    @Autowired
    private final PaymentMethodRepository paymentMethodRepository;

    public OrderServiceImpl(UserRepository userRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentService paymentService, CartItemService cartItemService, EmailService emailService, InvoiceService invoiceService, PaymentMethodRepository paymentMethodRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Override
    public OrderResponseDTO createMyOrder(Long userId, Long paymentMethodId) {
        Users users = userRepository.findById(userId)
                                        .orElseThrow(() -> new FindingUserError("user is not found"));
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new PaymentException("Method not found"));
        Cart cart = users.getCart();
        Order order = new Order();
        order.setCreateAt(new Date());
        order.setStatus(OrderStatus.PENDING);
        order.setUser(users);
        BigDecimal totalPrice = BigDecimal.ZERO;
        for(CartItem item : cart.getItems()) {
            totalPrice = totalPrice.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalPrice(totalPrice);
        Set<OrderItem> orderItems = cart.getItems().stream().map(
            item -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setPrice(item.getProduct().getPrice());
                orderItem.setProduct(item.getProduct());
                orderItem.setQuantity(item.getQuantity());
                orderItemRepository.save(orderItem);
                return orderItem;
            }
        ).collect(Collectors.toSet());
        Set<CartItemDetailsResponseDTO> cartItemDetailsResponseDTOs = cart.getItems().stream().map(
            item -> {
                CartItemDetailsResponseDTO dto = new CartItemDetailsResponseDTO();
                dto.setProductName(item.getProduct().getProductName());
                dto.setPrice(item.getProduct().getPrice());
                dto.setQuantity(item.getQuantity());
                return dto;
            }
        ).collect(Collectors.toSet());
        order.setOrderItems(orderItems);
        orderRepository.save(order);
        return new OrderResponseDTO(
            cartItemDetailsResponseDTOs,
            totalPrice,
            OrderStatus.PENDING
        );
    }

    @Override
    @Transactional
    public OrderMessageResponseDTO deleteOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                                        .orElseThrow(() -> new OrderExistsException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderValidException("You can't delete this order ;-;");
        }
        orderRepository.delete(order);
        return new OrderMessageResponseDTO("Delete order successful");
    }
}
