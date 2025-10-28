package com.trash.ecommerce.service;

import java.util.List;
import com.trash.ecommerce.entity.Users;

public interface UserService {
    public List<Users> findAllUser();
    public Users register(UserLoginDTO);
    public Users findUsersById(Long id);
    public Users updateUser(Users user, Long id);
    public Users deleteUser(Long id);
}
