package com.trash.ecommerce.service;

import java.util.List;

import com.trash.ecommerce.dto.UserLoginRequestDTO;
import com.trash.ecommerce.dto.UserLoginResponseDTO;
import com.trash.ecommerce.dto.UserRegisterRequestDTO;
import com.trash.ecommerce.dto.UserRegisterResponseDTO;
import com.trash.ecommerce.dto.UserResponseDTO;
import com.trash.ecommerce.dto.UserUpdateRequestDTO;
import com.trash.ecommerce.entity.Users;

public interface UserService {
    public List<Users> findAllUser();
    public UserRegisterResponseDTO register(UserRegisterRequestDTO user);
    public UserLoginResponseDTO login(UserLoginRequestDTO user);
    public Users findUsersById(Long id);
    public UserResponseDTO  updateUser(UserUpdateRequestDTO  user, Long id);
    public void deleteUser(Long id);
}
