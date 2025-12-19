package com.trash.ecommerce.mapper;

import com.trash.ecommerce.dto.UserProfileDTO;
import com.trash.ecommerce.entity.Role;
import com.trash.ecommerce.entity.Users;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserProfileDTO mapToUserProfileDTO(Users users) {
        return new UserProfileDTO(
                users.getId(),
                users.getEmail(),
                users.getAddress(),
                users.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet())
        );
    }
}
