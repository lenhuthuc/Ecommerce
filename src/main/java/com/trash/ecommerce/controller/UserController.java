package com.trash.ecommerce.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trash.ecommerce.dto.*;
import com.trash.ecommerce.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<Users>> getAllUsers(
        @RequestParam(value = "noPage", defaultValue = "0", required = false) int noPage,
        @RequestParam(value = "sizePage", defaultValue = "20", required = false) int sizePage
    ) {
        try {
            List<Users> users = userService.findAllUser(noPage, sizePage);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
        
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDTO> createUser(
        @Valid @RequestBody UserRegisterRequestDTO userRegisterRequestDTO,
        BindingResult result  
    ) {
        if(result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(new UserRegisterResponseDTO(errors.toString()));
        }
        UserRegisterResponseDTO userRegisterResponseDTO = new UserRegisterResponseDTO();
        try {
            userRegisterResponseDTO = userService.register(userRegisterRequestDTO);
            return ResponseEntity.ok(userRegisterResponseDTO);
        } catch (Exception e) {
            userRegisterResponseDTO.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(userRegisterResponseDTO);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginRequestDTO userLoginRequestDTO, 
    BindingResult result) {
        UserLoginResponseDTO userLoginResponseDTO = new UserLoginResponseDTO();
        if(result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
            );
            userLoginResponseDTO.setMessage(errors.toString());
            return ResponseEntity.badRequest().body(userLoginResponseDTO);
        }
        try {
            userLoginResponseDTO = userService.login(userLoginRequestDTO);
            return ResponseEntity.ok(userLoginResponseDTO);
        } catch (Exception e) {
            userLoginResponseDTO.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(userLoginResponseDTO);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping("/updation/{id}")
    public ResponseEntity<UserResponseDTO> putMethodName(
        @PathVariable String id, 
        @RequestHeader String token,
        @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO,
        BindingResult result
    ) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        if(result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
            );
            userResponseDTO.setMessage(errors.toString());
            return ResponseEntity.badRequest().body(userResponseDTO);
        }
        try {
            userResponseDTO = userService.updateUser(userUpdateRequestDTO, Long.parseLong(id), token);
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            userResponseDTO.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(userResponseDTO);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/details")
    public ResponseEntity<Users> findUser(@RequestParam(required = true) Long id) {
        try {
            Users user = userService.findUsersById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/removation")
    public ResponseEntity<UserResponseDTO> deleteUser(
        @RequestParam(required = true) Long id,
        @RequestHeader String token
    ) {
        try {
            userService.deleteUser(id, token);
            return ResponseEntity.ok(new UserResponseDTO("Succesful"));
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@RequestHeader String token) {
        try {
            UserProfileDTO userProfileDTO = userService.getOwnProfile(token);
            return ResponseEntity.ok(userProfileDTO);
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<Token> refreshToken(@RequestHeader String refreshToken) {
        try {
            Token token = jwtService.refreshToken(refreshToken);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(token);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

    }

}