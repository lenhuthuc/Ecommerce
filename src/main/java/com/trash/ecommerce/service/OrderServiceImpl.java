package com.trash.ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final PaymentService paymentService;
    @Autowired
    private final CartItemService cartItemService;
    @Autowired
    private final EmailService emailService;
    @Autowired
    private final InvoiceService invoiceService;
    @Autowired
    private final PaymentMethodRepository paymentMethodRepository;

    public OrderServiceImpl(UserRepository userRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentService paymentService, CartItemService cartItemService, EmailService emailService, InvoiceService invoiceService, PaymentMethodRepository paymentMethodRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentService = paymentService;
        this.cartItemService = cartItemService;
        this.emailService = emailService;
        this.invoiceService = invoiceService;
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
    public OrderMessageResponseDTO finalizeOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExistsException("Order not found"));
        Users user = order.getUser();
        Long userId = user.getId();
        order.setStatus(OrderStatus.PAID);
        Cart cart = user.getCart();
        for(CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getQuantity() - cartItem.getQuantity() >= 0) {
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            } else {
                throw new ProductQuantityValidation("Quantity is invalidation !");
            }
            cartItemService.removeItemOutOfCart(user.getId(), cartItem.getProduct().getId());
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
        invoiceService.createInvoice(userId, orderId, order.getPaymentMethod().getId());
        return new OrderMessageResponseDTO("Procedure is complete");
    }

}
