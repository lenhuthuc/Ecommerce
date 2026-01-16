package com.trash.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import com.trash.ecommerce.dto.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private EmailService emailService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Test
    void testResetPasswordStoresOtpAndSendsEmail() {
        String email = "test@example.com";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        UserResponseDTO response = userService.resetPassword(email);

        assertEquals("OTP has been send", response.getMessage());
        verify(valueOperations, times(1)).set(startsWith("otp:" + email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(emailService, times(1)).sendEmail(eq(email), eq("Reset Password"), contains("Your otp code"));
    }

    @Test
    void testResetPasswordWithInvalidEmailThrows() {
        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword(""));
    }
}
