package com.trash.ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.trash.ecommerce.entity.*;
import com.trash.ecommerce.exception.ProductQuantityValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.dto.OrderMessageResponseDTO;
import com.trash.ecommerce.dto.OrderResponseDTO;
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
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private EmailService emailService;
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

    @Override
    public OrderMessageResponseDTO finalizeOrder(Long userId, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExistsException("Order not found"));
        Users user = order.getUser();
        if(userId.equals(user.getId())) throw new OrderValidException("User not valid");
        order.setStatus(OrderStatus.PAID);
        Cart cart = user.getCart();
        for(CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() - cartItem.getQuantity() >= 0) {
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            } else {
                throw new ProductQuantityValidation("Quantity is invalidation !");
            }
            cartItemService.removeItemOutOfCart(user.getId(), cartItem.getId());
        }
        orderRepository.save(order);
        String subject = "Confirm the order transaction";
        String body = String.format(
                "Hi %s!\n\n" +
                        "We’ve successfully received your order #%s, and it’s now on its way to your doorstep " +
                        "(unless the universe decides to play tricks, but let’s hope not 😅).\n\n" +
                        "Get ready to enjoy your purchase soon! If anything goes wrong, don’t worry — our team is armed " +
                        "with coffee and a few clicks of magic 💻☕.\n\n" +
                        "Thanks for choosing us and placing your order — you just helped us secure our morning caffeine fix!\n\n" +
                        "Cheers,\n" +
                        "The Shop Team",
                user.getEmail(), orderId
        );
        emailService.sendEmail(user.getEmail(), subject, body);
        return new OrderMessageResponseDTO("Procedure is complete");
    }

}
