package com.trash.ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.dto.OrderMessageResponseDTO;
import com.trash.ecommerce.dto.OrderResponseDTO;
import com.trash.ecommerce.entity.Cart;
import com.trash.ecommerce.entity.CartItem;
import com.trash.ecommerce.entity.Order;
import com.trash.ecommerce.entity.OrderItem;
import com.trash.ecommerce.entity.OrderStatus;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.OrderExistsException;
import com.trash.ecommerce.exception.OrderValidException;
import com.trash.ecommerce.repository.OrderItemRepository;
import com.trash.ecommerce.repository.OrderRepository;
import com.trash.ecommerce.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private PaymentService paymentService;
    @Override
    public OrderResponseDTO createMyOrder(Long userId) {
        Users users = userRepository.findById(userId)
                                        .orElseThrow(() -> new FindingUserError("user is not found"));
        Cart cart = users.getCart();
        Order order = new Order();
        order.setCreateAt(LocalDateTime.now());
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

    @Override
    public OrderMessageResponseDTO checkOutOrder(Long userId, Long orderId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExistsException("Order not found"));
        if(!order.getUser().getId().equals(userId)) throw new OrderValidException("This user do not have authorization to this order");
        String paymentUrl = paymentService.createPaymentUrl(order.getTotalPrice(), "Transaction payment", order, ipAddress);
        return new OrderMessageResponseDTO(paymentUrl);
    }

}
