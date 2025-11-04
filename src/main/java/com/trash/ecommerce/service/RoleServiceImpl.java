package com.trash.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trash.ecommerce.entity.Role;
import com.trash.ecommerce.repository.RoleRepository;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;
    @Override
    public Role findRoleByName(String name) {
        return roleRepository.findByRoleName(name).orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));
    }

}
