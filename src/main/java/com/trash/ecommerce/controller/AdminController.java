package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.ProductRequireDTO;
import com.trash.ecommerce.dto.ProductResponseDTO;
import com.trash.ecommerce.dto.UserProfileDTO;
import com.trash.ecommerce.dto.UserResponseDTO;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.ProductCreatingException;
import com.trash.ecommerce.service.ProductService;
import com.trash.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    // ========== USER MANAGEMENT ==========
    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers(
            @RequestParam(value = "noPage", defaultValue = "0", required = false) int noPage,
            @RequestParam(value = "sizePage", defaultValue = "20", required = false) int sizePage
    ) {
        try {
            List<UserProfileDTO> users = userService.findAllUser(noPage, sizePage);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Users> findUser(@PathVariable Long id) {
        try {
            Users user = userService.findUsersById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")  // ← SỬA THÀNH /users/{id}
    public ResponseEntity<UserResponseDTO> deleteUser(
            @PathVariable Long id,
            @RequestHeader String token
    ) {
        try {
            userService.deleteUser(id, token);
            return ResponseEntity.ok(new UserResponseDTO("Succesful"));
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
    }

    // ========== PRODUCT MANAGEMENT ==========
    @PostMapping("/products")
    public ResponseEntity<ProductResponseDTO> addProduct(
            @RequestBody ProductRequireDTO productRequireDTO,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            ProductResponseDTO productResponseDTO = productService.createProduct(productRequireDTO, file);
            return ResponseEntity.ok(productResponseDTO);
        } catch (Exception e) {
            throw new ProductCreatingException(e.getMessage());
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @RequestBody ProductRequireDTO productRequireDTO,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            ProductResponseDTO productResponseDTO = productService.updateProduct(productRequireDTO, id, file);
            return ResponseEntity.ok(productResponseDTO);
        } catch (Exception e) {
            throw new ProductCreatingException(e.getMessage());
        }
    }

    @DeleteMapping("/products/{id}")  // ← SỬA THÀNH /products/{id}
    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable Long id) {
        try {
            ProductResponseDTO productResponseDTO = productService.deleteProductById(id);
            return ResponseEntity.ok(productResponseDTO);
        } catch (Exception e) {
            throw new ProductCreatingException(e.getMessage());
        }
    }
}