package com.trash.ecommerce.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.trash.ecommerce.dto.OrderSummaryDTO;
import com.trash.ecommerce.entity.*;
import com.trash.ecommerce.exception.*;
import com.trash.ecommerce.mapper.OrderMapper;
import com.trash.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trash.ecommerce.dto.CartItemDetailsResponseDTO;
import com.trash.ecommerce.dto.OrderMessageResponseDTO;
import com.trash.ecommerce.dto.OrderResponseDTO;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private OrderMapper orderMapper;
    public OrderServiceImpl(UserRepository userRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentService paymentService, CartItemService cartItemService, EmailService emailService, InvoiceService invoiceService, PaymentMethodRepository paymentMethodRepository, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public List<OrderSummaryDTO> getAllMyOrders(Long userId, String ipAddress) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreateAtDesc(userId);

        return orders.stream().map(order -> {
            String url = null;
            if (order.getStatus() == OrderStatus.PENDING_PAYMENT && order.getPaymentMethod().getId() == 2) {
                url = paymentService.createPaymentUrl(order.getTotalPrice(), ".", order.getId(), ipAddress);
            }

            return orderMapper.toOrderSummaryDTO(order, url);
        }).collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrderById(Long userId, Long orderId, String IpAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return orderMapper.toOrderResponseDTO(
                order,
                (order.getPaymentMethod().getId() == 1) ? null : paymentService.createPaymentUrl(order.getTotalPrice(), ".", order.getId(), IpAddress)
        );
    }

    @Override
    @Transactional
    public OrderResponseDTO createMyOrder(Long userId, Long paymentMethodId, String IpAddress) {


        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new FindingUserError("User not found"));

        Cart cart = user.getCart();
        if (cart == null || cart.getItems().isEmpty()) {
            throw new OrderValidException("Cart is empty");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new PaymentException("Payment method not found"));

        String address = user.getAddress();
        // 2. Khởi tạo Order
        Order order = new Order();
        order.setCreateAt(new Date());
        order.setUser(user);
        order.setPaymentMethod(paymentMethod);
        order.setAddress(address);
        if (paymentMethod.getId() == 2L) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
        } else {
            order.setStatus(OrderStatus.PLACED);
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        Set<OrderItem> orderItems = new HashSet<>();
        Set<CartItemDetailsResponseDTO> responseItems = new HashSet<>();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            Long quantityBuy = cartItem.getQuantity();

            if (product.getQuantity() < quantityBuy) {
                throw new ProductQuantityValidation("Product " + product.getProductName() + " is out of stock!");
            }
            product.setQuantity(product.getQuantity() - quantityBuy);

            BigDecimal currentPrice = product.getPrice();
            BigDecimal lineAmount = currentPrice.multiply(BigDecimal.valueOf(quantityBuy));
            totalPrice = totalPrice.add(lineAmount);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantityBuy);
            orderItem.setPrice(currentPrice);
            orderItems.add(orderItem);


            CartItemDetailsResponseDTO dto = new CartItemDetailsResponseDTO();
            dto.setProductName(product.getProductName());
            dto.setPrice(currentPrice);
            dto.setQuantity(quantityBuy);
            responseItems.add(dto);
        }

        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        orderRepository.save(order);

        cartRepository.deleteCartItems(cart.getId());
        cart.getItems().clear();
        return new OrderResponseDTO(
                responseItems,
                totalPrice,
                order.getStatus(),
                address,
                (paymentMethodId == 1) ? null : paymentService.createPaymentUrl(totalPrice, ".", order.getId(), IpAddress)
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
