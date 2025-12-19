package com.trash.ecommerce.service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.trash.ecommerce.config.RedisConfig;
import com.trash.ecommerce.dto.*;
import com.trash.ecommerce.entity.Role;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.UserAuthorizationException;
import com.trash.ecommerce.mapper.UserMapper;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    @Autowired
    private UserMapper userMapper;
    @Override
    public List<UserProfileDTO> findAllUser(int noPage, int sizePage) {
        PageRequest pageRequest = PageRequest.of(noPage, sizePage);
        List<UserProfileDTO> users = userRepository.findAll(pageRequest).getContent()
                .stream().map(user -> userMapper.mapToUserProfileDTO(user)).toList();
        return users;
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
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.findRoleByName("USER"));
        tmpUser.setRoles(roles);
        Cart cart = new Cart();
        cart.setUser(tmpUser);
        tmpUser.setCart(cart);
        cartRepository.save(cart);
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
            return new UserLoginResponseDTO(token, "Bearer", jwtService.extractExpiration(token.getRefresh()), "Succesful");
        }
        return new UserLoginResponseDTO(new Token(null,null), null, null, "Sai email/mật khẩu");
    }

    @Override
    public UserResponseDTO logout(Long userId) {
        String key = "refresh:" + String.valueOf(userId);
        redisTemplate.delete(key);
        return new UserResponseDTO("Success");
    }

    @Override
    public UserProfileDTO findUsersById(Long id) {
        Users users = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        UserProfileDTO userProfileDTO = userMapper.mapToUserProfileDTO(users);
        return userProfileDTO;
    }

    @Override
    public UserProfileDTO getOwnProfile(Long id) {
        Users user = userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("User is not found"));
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setId(user.getId());
        userProfileDTO.setEmail(user.getEmail());
        userProfileDTO.setAddress(user.getAddress());
        Set<String> roles = new HashSet<>();
        for (Role role : user.getRoles()) {
            roles.add(role.getRoleName());
        }
        userProfileDTO.setRoles(roles);
        return userProfileDTO;
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UserUpdateRequestDTO user, Long id, Long userId) {
        Users currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));

        Set<String> roles = new HashSet<>();
        for (Role role : currentUser.getRoles()) {
            roles.add(role.getRoleName());
        }

        if (!id.equals(userId) && !roles.contains("ADMIN")) {
            throw new FindingUserError("Not valid");
        }

        Users targetUser = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            targetUser.setEmail(user.getEmail());
        }

        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            targetUser.setAddress(user.getAddress());
        }


        userRepository.save(targetUser);
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
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));
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
        redisTemplate.opsForValue().set("otp:" + email, String.valueOf(number), 5, TimeUnit.MINUTES);
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
        userRepository.save(user);
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
