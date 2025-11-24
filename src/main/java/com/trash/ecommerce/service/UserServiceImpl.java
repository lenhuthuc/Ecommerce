package com.trash.ecommerce.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.trash.ecommerce.config.RedisConfig;
import com.trash.ecommerce.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trash.ecommerce.entity.Cart;
import com.trash.ecommerce.entity.Invoice;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.repository.CartRepository;
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
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private EmailService emailService;
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
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
        tmpUser.setEmail(user.getEmail());
        String password = en.encode(user.getPassword());
        tmpUser.setPassword(password);
        tmpUser.setRoles(Set.of(roleService.findRoleByName("USER")));
        Cart cart = new Cart();
        cart.setUser(tmpUser);
        cartRepository.save(cart);
        tmpUser.setCart(cart);
        userRepository.save(tmpUser);
        return new UserRegisterResponseDTO("Đăng kí thành công");
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequestDTO user) {
        Authentication authentication = auth
                .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            Users u = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new RuntimeException("user is not found"));
            Token token = jwtService.generateToken(user.getEmail(), u.getId());
            return new UserLoginResponseDTO(token, "Bearer", jwtService.extractExpiration(token.getAccess()), "Succesful");
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
                GrantedAuthority::getAuthority
        ).collect(Collectors.toSet());
        if (!roles.contains("ADMIN") && !Objects.equals(jwtService.extractId(token), id)) {
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
                GrantedAuthority::getAuthority
        ).collect(Collectors.toSet());
        if (!roles.contains("ADMIN") && !Objects.equals(jwtService.extractId(token), id)) {
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
        
        userRepository.delete(user);
    }

    @Override
    public UserResponseDTO resetPassword(String email) {
        int number = (int)(Math.random() * 900000) + 100000;
        redisTemplate.opsForValue().set("opt:" + email, String.valueOf(number), 5, TimeUnit.MINUTES);
        emailService.sendEmail(email, "Reset Password", "Your otp code is : " + number);
        return new UserResponseDTO("OTP has been send");
    }

    @Override
    public boolean verifyDTO(String email, String OTP) {
        String key = "otp:" + email;
        String storeOtp = (String) redisTemplate.opsForValue().get(key);
        if (storeOtp == null) return false;
        boolean valid = storeOtp.equals(OTP);
        if(valid) redisTemplate.delete(key);
        return valid;
    }

    @Override
    public UserResponseDTO changePassword(String email, String newPassword) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User is not found"));
        newPassword = en.encode(newPassword);
        user.setPassword(newPassword);
        return new UserResponseDTO("Change password successfully");
    }

    @Override
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
