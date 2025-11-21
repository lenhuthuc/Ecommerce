package com.trash.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class PaymentHashGenerator {
    @Autowired
    private VnPayConfig vnPayConfig;
    public String HmacSHA512(String secretKey,String data ) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(keySpec);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public String hashAllFields(Map<String, String> fields) {
        StringBuilder query = new StringBuilder();
        Iterator<String> idx = fields.keySet().iterator();
        while (idx.hasNext()) {
            String fieldName = idx.next();
            String fileValue = fields.get(fieldName);
            if (!query.isEmpty()) query.append('&');
            query.append(fieldName).append('=').append(fileValue);
        }
        return HmacSHA512(vnPayConfig.getHashSecret(), query.toString());
    }
}
