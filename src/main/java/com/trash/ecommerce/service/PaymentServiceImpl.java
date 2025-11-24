package com.trash.ecommerce.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import com.trash.ecommerce.config.PaymentHashGenerator;
import com.trash.ecommerce.config.VnPayConfig;
import com.trash.ecommerce.dto.PaymentMethodMessageResponse;
import com.trash.ecommerce.dto.PaymentMethodResponse;
import com.trash.ecommerce.entity.*;
import com.trash.ecommerce.exception.OrderExistsException;
import com.trash.ecommerce.exception.PaymentException;
import com.trash.ecommerce.exception.ProductQuantityValidation;
import com.trash.ecommerce.repository.CartRepository;
import com.trash.ecommerce.repository.OrderRepository;
import com.trash.ecommerce.repository.PaymentMethodRepository;
import com.trash.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private VnPayConfig vnPayConfig;
    @Autowired
    private PaymentHashGenerator paymentHashGenerator;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private OrderService orderService;

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
    public String createPaymentUrl(BigDecimal total_price, String orderInfo, Order order, String ipAddress) {
        String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_OrderInfo = orderInfo;
            String orderType = "100000";
            String vnp_TxnRef = String.valueOf(order.getId());
            String vnp_IpAddr = ipAddress;
            String vnp_TmnCode = vnPayConfig.getTmnCode();
    
            int amount = Integer.parseInt(total_price.toPlainString())*100;
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

            Order order = orderRepository.findById(Long.valueOf(fields.get("vnp_TxnRef")))
                    .orElseThrow(() -> new OrderExistsException("Order not found"));
            long vnpAmount = Long.parseLong(fields.get("vnp_Amount")) / 100;
            boolean checkAmount = order.getTotalPrice().equals(BigDecimal.valueOf(vnpAmount));
            boolean checkOrderStatus = order.getStatus() == OrderStatus.PENDING;


            if(order.getId() != null)
            {
                if(checkAmount)
                {
                    if (checkOrderStatus)
                    {
                        if ("00".equals(request.getParameter("vnp_ResponseCode")))
                        {
                            orderService.finalizeOrder(Long.valueOf(fields.get("vnp_TxnRef")));
                        }
                        else
                        {
                            throw new PaymentException("Transaction has not been successful");
                        }
                        return new PaymentMethodResponse("01","Confirm Success");
                    }
                    else
                    {
                        return new PaymentMethodResponse("02","rder already confirmed");
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
