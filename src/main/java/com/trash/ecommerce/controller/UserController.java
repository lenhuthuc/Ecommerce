package com.trash.ecommerce.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trash.ecommerce.dto.*;
import com.trash.ecommerce.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger logger = LoggerFactory.getLogger(ReviewController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("auth/register")
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
            logger.error("Đăng kí thất bại ",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("auth/login")
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

    @PostMapping("auth/logout")
    public ResponseEntity<UserResponseDTO> logout(
            @RequestHeader("Authorization") String token
    ) {
        System.out.println("LOGOUT");
        Long userId = jwtService.extractId(token);
        try {
            com.trash.ecommerce.dto.UserResponseDTO userResponseDTO = userService.logout(userId);
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updation/{id}")
    public ResponseEntity<UserResponseDTO> putMethodName(
        @PathVariable Long id,
        @RequestHeader("Authorization") String token,
        @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO,
        BindingResult result
    ) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        if(result.hasErrors()) {
            System.out.println(">>>>putMethodName has some errors");
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
            );
            userResponseDTO.setMessage(errors.toString());
            return ResponseEntity.badRequest().body(userResponseDTO);
        }
        try {
            Long userId = jwtService.extractId(token);
            userResponseDTO = userService.updateUser(userUpdateRequestDTO, id, userId);
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            userResponseDTO.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(userResponseDTO);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = jwtService.extractId(token);
        System.out.println("getProfile");
        try {

            UserProfileDTO userProfileDTO = userService.getOwnProfile(userId);
            return ResponseEntity.ok(userProfileDTO);
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<Token> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        try {
            Token token = jwtService.refreshToken(refreshToken);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return ResponseEntity.ok(token);
        } catch (ExpiredJwtException e) {
            logger.error("ExpiredJwtException", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

    }

}