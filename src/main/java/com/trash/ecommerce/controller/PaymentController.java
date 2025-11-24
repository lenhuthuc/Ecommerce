package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.PaymentMethodMessageResponse;
import com.trash.ecommerce.dto.PaymentMethodResponse;
import com.trash.ecommerce.service.JwtService;
import com.trash.ecommerce.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private Logger logger = LoggerFactory.getLogger(PaymentController.class);
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private JwtService jwtService;

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