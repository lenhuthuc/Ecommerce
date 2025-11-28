package com.trash.ecommerce.service;

import java.util.List;

import com.trash.ecommerce.dto.UserLoginRequestDTO;
import com.trash.ecommerce.dto.UserLoginResponseDTO;
import com.trash.ecommerce.dto.UserProfileDTO;
import com.trash.ecommerce.dto.UserRegisterRequestDTO;
import com.trash.ecommerce.dto.UserRegisterResponseDTO;
import com.trash.ecommerce.dto.UserResponseDTO;
import com.trash.ecommerce.dto.UserUpdateRequestDTO;
import com.trash.ecommerce.entity.Users;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    public List<UserProfileDTO> findAllUser(int noPage, int sizePage);
    public UserRegisterResponseDTO register(UserRegisterRequestDTO user);
    public UserLoginResponseDTO login(UserLoginRequestDTO user);
    public UserResponseDTO logout(Long userId);
    public UserProfileDTO findUsersById(Long id);
    public UserProfileDTO getOwnProfile(Long userId);
    public UserResponseDTO  updateUser(UserUpdateRequestDTO  user, Long id, Long userId);
    public void deleteUser(Long id, String token);
    public UserResponseDTO resetPassword(String email);
    public boolean verifyDTO(String email, String OTP);
    public UserResponseDTO changePassword(String email, String newPassword);
    public String getClientIpAddress(HttpServletRequest request);
}
