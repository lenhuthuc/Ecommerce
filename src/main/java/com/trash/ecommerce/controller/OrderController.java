package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.OrderMessageResponseDTO;
import com.trash.ecommerce.dto.OrderResponseDTO;
import com.trash.ecommerce.dto.OrderSummaryDTO;
import com.trash.ecommerce.service.JwtService;
import com.trash.ecommerce.service.OrderService;
import com.trash.ecommerce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private OrderService orderService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderSummaryDTO>> getMyOrders(
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        try {
            Long userId = jwtService.extractId(token);
            String ipAddress = userService.getClientIpAddress(request);
            List<OrderSummaryDTO> orders = orderService.getAllMyOrders(userId, ipAddress);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Generating order has some errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            Long userId = jwtService.extractId(token);
            String ipAddress = userService.getClientIpAddress(request);
            OrderResponseDTO orderDetail = orderService.getOrderById(userId, orderId, ipAddress);

            return ResponseEntity.ok(orderDetail);
        } catch (Exception e) {
            logger.error("Generating order has some errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestHeader("Authorization") String token,
            @RequestParam("paymentMethod") Long paymentMethodId,
            HttpServletRequest request) {
        try {
            Long userId = jwtService.extractId(token);
            OrderResponseDTO order = orderService.createMyOrder(userId, paymentMethodId, userService.getClientIpAddress(request));
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Generating order has some errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderMessageResponseDTO> deleteOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId) {
        try {
            Long userId = jwtService.extractId(token);
            OrderMessageResponseDTO response = orderService.deleteOrder(userId, orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Deleting order has some errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}