package com.trash.ecommerce.dto;

import java.util.Set;

import com.trash.ecommerce.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserProfileDTO {
    private Long id;
    private String email;
    private String address;
    private Set<String> roles;
}
