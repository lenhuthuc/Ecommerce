package com.trash.ecommerce.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trash.ecommerce.dto.UserLoginRequestDTO;
import com.trash.ecommerce.dto.UserLoginResponseDTO;
import com.trash.ecommerce.dto.UserProfileDTO;
import com.trash.ecommerce.dto.UserRegisterRequestDTO;
import com.trash.ecommerce.dto.UserRegisterResponseDTO;
import com.trash.ecommerce.dto.UserResponseDTO;
import com.trash.ecommerce.dto.UserUpdateRequestDTO;
import com.trash.ecommerce.entity.Invoice;
import com.trash.ecommerce.entity.Role;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder en;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthenticationManager auth;
    @Autowired
    private JwtService jwtService;

    @Override
    public List<Users> findAllUser(int noPage, int sizePage) {
        PageRequest pageRequest = PageRequest.of(noPage, noPage);
        Page<Users> users = userRepository.findAll(pageRequest);
        return users.getContent();
    }

    @Override
    public UserRegisterResponseDTO register(UserRegisterRequestDTO user) {
        Users tmpUser = new Users();
        String email = user.getEmail();
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email đã tồn tại");
        }
        tmpUser.setEmail(user.getEmail());
        String password = en.encode(user.getPassword());
        tmpUser.setPassword(password);
        tmpUser.setRoles(Set.of(roleService.findRoleByName("USER")));
        userRepository.save(tmpUser);
        return new UserRegisterResponseDTO("Đăng kí thành công");
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequestDTO user) {
        Authentication authentication = auth
                .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            Users u = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new RuntimeException("user is not found"));
            String token = jwtService.generateToken(user.getEmail(), u.getId());
            return new UserLoginResponseDTO(token, "Bearer", jwtService.extractExpiration(token), "Succesful");
        } else
            throw new RuntimeException("Sai email/mật khẩu");
    }

    @Override
    public Users findUsersById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    @Override
    public UserProfileDTO getOwnProfile(String token) {
        Long id = jwtService.extractId(token);
        Users user = userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("User is not found"));
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setEmail(user.getEmail());
        userProfileDTO.setPassword(user.getPassword());
        userProfileDTO.setAddress(user.getAddress());
        userProfileDTO.setRoles(user.getRoles());
        return userProfileDTO;
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UserUpdateRequestDTO user, Long id, String token) {
        Authentication authorities = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = authorities.getAuthorities().stream().map(
            role -> role.getAuthority()
        ).collect(Collectors.toSet());
        if (!roles.contains("ADMIN") && jwtService.extractId(token) != id) {
            return new UserResponseDTO("Fail");
        }
        Users tmpUser = findUsersById(id);
        tmpUser.setEmail(user.getEmail());
        String password = en.encode(user.getPassword());
        tmpUser.setPassword(password);
        tmpUser.setAddress(user.getAddress());
        tmpUser.setPaymentMethods(user.getPaymentMethod());
        userRepository.save(tmpUser);
        return new UserResponseDTO("Update thành công");
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String token) {
        Authentication authorities = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = authorities.getAuthorities().stream().map(
            role -> role.getAuthority()
        ).collect(Collectors.toSet());
        if (!roles.contains("ADMIN") && jwtService.extractId(token) != id) {
            return;
        }
        Users user = findUsersById(id);
        if (user == null) {
            throw new RuntimeException("User not found with id " + id);
        }
        // Ngắt quan hệ ManyToMany
        user.getRoles().clear();
        user.getPaymentMethods().clear();
        // Ngắt OneToOne
        if (user.getCart() != null) {
            user.getCart().setUser(null);
            user.setCart(null);
        }
        // Ngắt OneToMany
        for (Invoice invoice : user.getInvoices()) {
            invoice.setUser(null);
        }
        user.getInvoices().clear();
        userRepository.delete(user);
    }

}
