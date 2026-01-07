package com.trash.ecommerce.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import com.trash.ecommerce.config.PaymentHashGenerator;
import com.trash.ecommerce.config.VnPayConfig;
import com.trash.ecommerce.dto.OrderRequest;
import com.trash.ecommerce.dto.PaymentMethodMessageResponse;
import com.trash.ecommerce.dto.PaymentMethodResponse;
import com.trash.ecommerce.entity.*;
import com.trash.ecommerce.exception.OrderExistsException;
import com.trash.ecommerce.exception.PaymentException;
import com.trash.ecommerce.exception.ProductQuantityValidation;
import com.trash.ecommerce.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private VnPayConfig vnPayConfig;
    @Autowired
    private PaymentHashGenerator paymentHashGenerator;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private InvoiceService invoiceService;

    @Override
    public PaymentMethodMessageResponse addPaymentMethod(Long userId, String name) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setMethodName(name);

        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);

        return new PaymentMethodMessageResponse("success");
    }

    @Override
    public String createPaymentUrl(BigDecimal total_price, String orderInfo, Long orderId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderExistsException("Order not found"));
        String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_OrderInfo = orderInfo;
            String orderType = "100000";
            String vnp_TxnRef = String.valueOf(order.getId());
            String vnp_IpAddr = ipAddress;
            String vnp_TmnCode = vnPayConfig.getTmnCode();
    
            // Chuyển BigDecimal sang VND (nhân 100 và làm tròn)
            long amount = total_price.multiply(BigDecimal.valueOf(100)).longValue();
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
    
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
    
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            //Add Params of 2.1.0 Version
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();

            String vnp_SecureHash = paymentHashGenerator.HmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnPayConfig.getUrl() + "?" + queryUrl;
    }

    @Override
    public Map<String, String> hashFields(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Iterator<String> params = request.getParameterNames().asIterator();
        while (params.hasNext()) {
            String fieldName = params.next();
            String fieldValue = request.getParameter(fieldName);
            fieldName = URLEncoder.encode(fieldName, StandardCharsets.UTF_8);
            fieldValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        return fields;
    }

    @Override
    public PaymentMethodMessageResponse handleProcedureUserInterface(HttpServletRequest request) {
        Map<String, String> fields = hashFields(request);
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        String signValue = paymentHashGenerator.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                return new PaymentMethodMessageResponse("GD Thanh cong");
            } else {
                return new PaymentMethodMessageResponse("GD Khong thanh cong");
            }

        } else {
            return new PaymentMethodMessageResponse("Chu ky khong hop le");
        }
    }

    @Override
    public PaymentMethodResponse handleProcedurePayment(HttpServletRequest request) {
        Map<String, String> fields = hashFields(request);
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        String signValue = paymentHashGenerator.hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash))
        {

            String txnRef = fields.get("vnp_TxnRef");
            if (txnRef == null || txnRef.isEmpty()) {
                return new PaymentMethodResponse("03", "Invalid transaction reference");
            }
            
            Order order = orderRepository.findById(Long.valueOf(txnRef))
                    .orElseThrow(() -> new OrderExistsException("Order not found"));
            
            if (order.getUser() == null) {
                return new PaymentMethodResponse("05", "Order user not found");
            }
            
            Long orderId = order.getId();
            String vnpAmountStr = fields.get("vnp_Amount");
            if (vnpAmountStr == null || vnpAmountStr.isEmpty()) {
                return new PaymentMethodResponse("06", "Invalid amount");
            }
            
            long vnpAmountLong = Long.parseLong(vnpAmountStr);
            BigDecimal vnpAmount = BigDecimal.valueOf(vnpAmountLong).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            if (order.getTotalPrice() == null) {
                return new PaymentMethodResponse("07", "Order total price is null");
            }
            // So sánh BigDecimal bằng compareTo để tránh vấn đề precision
            boolean checkAmount = order.getTotalPrice().compareTo(vnpAmount) == 0;
            boolean checkOrderStatus = order.getStatus() == OrderStatus.PENDING_PAYMENT;


            if(order.getId() != null)
            {
                if(checkAmount)
                {
                    if (checkOrderStatus)
                    {
                        if ("00".equals(request.getParameter("vnp_ResponseCode")))
                        {
                            Users user = order.getUser();
                            if (user == null) {
                                throw new PaymentException("User not found for order");
                            }
                            Long userId = user.getId();
                            order.setStatus(OrderStatus.PAID);
                            
                            // Xử lý order items thay vì cart items (vì cart đã bị xóa khi tạo order)
                            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                                throw new PaymentException("Order has no items");
                            }
                            
                            for(OrderItem orderItem : order.getOrderItems()) {
                                if (orderItem == null || orderItem.getProduct() == null) {
                                    continue;
                                }
                                Product product = orderItem.getProduct();
                                Long quantity = orderItem.getQuantity();
                                if (quantity == null || quantity <= 0) {
                                    continue;
                                }
                                // Giảm stock từ order items
                                int updatedRows = productRepository.decreaseStock(product.getId(), quantity);
                                if (updatedRows == 0) {
                                    throw new ProductQuantityValidation("Hết hàng hoặc số lượng không đủ cho sản phẩm: " + product.getProductName());
                                }
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
                            if (order.getPaymentMethod() != null) {
                                invoiceService.createInvoice(userId, orderId, order.getPaymentMethod().getId());
                            } else {
                                throw new PaymentException("Payment method not found for order");
                            }
                        }
                        else
                        {
                            throw new PaymentException("Transaction has not been successful");
                        }
                        return new PaymentMethodResponse("01","Confirm Success");
                    }
                    else
                    {
                        return new PaymentMethodResponse("02","Order already confirmed");
                    }
                }
                else
                {
                    return new PaymentMethodResponse("04","Invalid Amount");
                }
            }
            else
            {
                return new PaymentMethodResponse("01","Order not Found");
            }
        }
        else
        {
            return new PaymentMethodResponse("97","Invalid Checksum");
        }
    }
}
