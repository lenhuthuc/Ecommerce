package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.UserResponseDTO;
import com.trash.ecommerce.service.JwtService;
import com.trash.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerPasswordResetTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
    }

    @Test
    public void testResetPasswordWithValidEmail() throws Exception {
        String email = "test@example.com";
        UserResponseDTO response = new UserResponseDTO("OTP has been send");
        
        when(userService.resetPassword(email)).thenReturn(response);

        mockMvc.perform(post("/api/user/auth/reset-password")
                .param("email", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("OTP has been send")));
    }

    @Test
    public void testResetPasswordWithInvalidEmail() throws Exception {
        String email = "";
        
        when(userService.resetPassword(email)).thenThrow(new IllegalArgumentException("Email is required"));

        mockMvc.perform(post("/api/user/auth/reset-password")
                .param("email", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testVerifyOTPWithValidCode() throws Exception {
        String email = "test@example.com";
        String otp = "123456";
        
        when(userService.verifyDTO(email, otp)).thenReturn(true);

        mockMvc.perform(post("/api/user/auth/verify-otp")
                .param("email", email)
                .param("otp", otp)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("OTP verified successfully")));
    }

    @Test
    public void testVerifyOTPWithInvalidCode() throws Exception {
        String email = "test@example.com";
        String otp = "999999";
        
        when(userService.verifyDTO(email, otp)).thenReturn(false);

        mockMvc.perform(post("/api/user/auth/verify-otp")
                .param("email", email)
                .param("otp", otp)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid or expired OTP")));
    }

    @Test
    public void testChangePasswordWithValidEmail() throws Exception {
        String email = "test@example.com";
        String newPassword = "NewPassword123!";
        UserResponseDTO response = new UserResponseDTO("Change password successfully");
        
        when(userService.verifyDTO(email, "")).thenReturn(true);
        when(userService.changePassword(email, newPassword)).thenReturn(response);

        mockMvc.perform(post("/api/user/auth/change-password")
                .param("email", email)
                .param("newPassword", newPassword)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Change password successfully")));
    }

    @Test
    public void testChangePasswordWithInvalidEmail() throws Exception {
        String email = "";
        String newPassword = "NewPassword123!";
        
        when(userService.changePassword(email, newPassword)).thenThrow(new IllegalArgumentException("Email is required"));

        mockMvc.perform(post("/api/user/auth/change-password")
                .param("email", email)
                .param("newPassword", newPassword)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testChangePasswordWithEmptyNewPassword() throws Exception {
        String email = "test@example.com";
        String newPassword = "";
        
        when(userService.changePassword(email, newPassword)).thenThrow(new IllegalArgumentException("New password is required"));

        mockMvc.perform(post("/api/user/auth/change-password")
                .param("email", email)
                .param("newPassword", newPassword)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testPasswordResetFlowSequence() throws Exception {
        String email = "test@example.com";
        String otp = "123456";
        String newPassword = "NewPassword123!";
        
        // Step 1: Request password reset
        UserResponseDTO resetResponse = new UserResponseDTO("OTP has been send");
        when(userService.resetPassword(email)).thenReturn(resetResponse);
        
        mockMvc.perform(post("/api/user/auth/reset-password")
                .param("email", email)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        // Step 2: Verify OTP
        when(userService.verifyDTO(email, otp)).thenReturn(true);
        
        mockMvc.perform(post("/api/user/auth/verify-otp")
                .param("email", email)
                .param("otp", otp)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        // Step 3: Change password
        UserResponseDTO changeResponse = new UserResponseDTO("Change password successfully");
        when(userService.changePassword(email, newPassword)).thenReturn(changeResponse);
        
        mockMvc.perform(post("/api/user/auth/change-password")
                .param("email", email)
                .param("newPassword", newPassword)
                .param("otp", otp)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
