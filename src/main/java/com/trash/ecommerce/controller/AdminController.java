package com.trash.ecommerce.controller;

import com.trash.ecommerce.dto.ProductRequestDTO;
import com.trash.ecommerce.dto.ProductResponseDTO;
import com.trash.ecommerce.dto.UserProfileDTO;
import com.trash.ecommerce.dto.UserResponseDTO;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.ProductCreatingException;
import com.trash.ecommerce.service.ProductService;
import com.trash.ecommerce.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping("api/admin")
public class AdminController {

    private Logger logger = LoggerFactory.getLogger(CartController.class);
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
    public ResponseEntity<UserProfileDTO> findUser(@PathVariable Long id) {
        try {
            UserProfileDTO user = userService.findUsersById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new FindingUserError(e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> deleteUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        try {
            userService.deleteUser(id, token);
            return ResponseEntity.ok(new UserResponseDTO("Succesful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== PRODUCT MANAGEMENT ==========
    @PostMapping("/products")
    public ResponseEntity<ProductResponseDTO> addProduct(
            @RequestPart("products") ProductRequestDTO productRequestDTO,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            ProductResponseDTO productResponseDTO = productService.createProduct(productRequestDTO, file);
            return ResponseEntity.ok(productResponseDTO);
        } catch (Exception e) {
            throw new ProductCreatingException(e.getMessage());
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @RequestPart("products") ProductRequestDTO productRequestDTO,
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            ProductResponseDTO productResponseDTO = productService.updateProduct(productRequestDTO, id, file);
            return ResponseEntity.ok(productResponseDTO);
        } catch (Exception e) {
            logger.error("Update product has some problem", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable Long id) {
        try {
            ProductResponseDTO productResponseDTO = productService.deleteProductById(id);
            return ResponseEntity.ok(productResponseDTO);
        } catch (Exception e) {
            throw new ProductCreatingException(e.getMessage());
        }
    }
}