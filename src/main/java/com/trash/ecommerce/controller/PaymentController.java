package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.OrderRequest;
import com.trash.ecommerce.dto.PaymentMethodMessageResponse;
import com.trash.ecommerce.dto.PaymentMethodResponse;
import com.trash.ecommerce.entity.Order;
import com.trash.ecommerce.service.JwtService;
import com.trash.ecommerce.service.PaymentService;
import com.trash.ecommerce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private Logger logger = LoggerFactory.getLogger(PaymentController.class);
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;

    @PostMapping("/createUrl")
    public ResponseEntity<String> createUrlVNPay(
            @RequestParam("totalPrice") BigDecimal total_price,
            @RequestParam("orderInfo") String orderInfo,
            @RequestParam("orderId") Long orderId,
            HttpServletRequest request
    ) {
        try {
            String ipAddress = userService.getClientIpAddress(request);
            String Url = paymentService.createPaymentUrl(total_price, orderInfo, orderId, ipAddress);
            return ResponseEntity.ok(Url);
        } catch (Exception e) {
            logger.error("Payment has errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/methods")
    public ResponseEntity<PaymentMethodMessageResponse> addPaymentMethod(
            @RequestHeader("Authorization") String token,
            @RequestParam String name) {
        try {
            Long userId = jwtService.extractId(token);
            PaymentMethodMessageResponse response = paymentService.addPaymentMethod(userId, name);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("addPaymentMethod has errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<PaymentMethodResponse> handleVnPayIPN(
            HttpServletRequest request) {
        try {
            PaymentMethodResponse response = paymentService.handleProcedurePayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("handleVnPayIPN has errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<PaymentMethodMessageResponse> handleVnPayReturn(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId =  jwtService.extractId(token);
            PaymentMethodMessageResponse response = paymentService.handleProcedureUserInterface(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("handleVnPayReturn has errors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}