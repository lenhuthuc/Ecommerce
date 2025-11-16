package com.trash.ecommerce.controller;

import org.springframework.web.bind.annotation.RestController;

import com.trash.ecommerce.dto.ProductDetailsResponseDTO;
import com.trash.ecommerce.dto.ProductRequireDTO;
import com.trash.ecommerce.dto.ProductResponseDTO;
import com.trash.ecommerce.exception.ProductCreatingException;
import com.trash.ecommerce.exception.ProductFingdingException;
import com.trash.ecommerce.service.ProductService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @GetMapping("/")
    public ResponseEntity<ProductDetailsResponseDTO> findProductById(
        @RequestParam Long id
    ) {
        try {
            ProductDetailsResponseDTO product = productService.findProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            throw new ProductFingdingException(e.getMessage());
        }
    }
    
    @GetMapping("/products")
    public ResponseEntity<List<ProductDetailsResponseDTO>> getAllProduct(
        @RequestParam(defaultValue = "0") int noPage,
        @RequestParam(defaultValue = "30") int sizePage
    ) {
        try {
            List<ProductDetailsResponseDTO> products = productService.findAllProduct(noPage, sizePage);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            throw new ProductFingdingException(e.getMessage());
        }
    }
    
    @GetMapping("/")
    public ResponseEntity<List<ProductDetailsResponseDTO>> findProductByName(
        @RequestParam String name,
        @RequestParam(defaultValue = "0") int noPage,
        @RequestParam(defaultValue = "30") int sizePage
    ) {
        try {
            List<ProductDetailsResponseDTO> products = productService.findProductByName(name, noPage, sizePage);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            throw new ProductFingdingException(e.getMessage());
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> addProduct(
        @RequestBody ProductRequireDTO productRequireDTO
        ) {
       try {
         ProductResponseDTO productResponseDTO = productService.createProduct(productRequireDTO);
         return ResponseEntity.ok(productResponseDTO);
       } catch (Exception e) {
         throw new ProductCreatingException(e.getMessage());
       }
    }
    
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
        @RequestBody ProductRequireDTO productRequireDTO,
        @RequestParam Long id
        ) {
       try {
         ProductResponseDTO productResponseDTO = productService.updateProduct(productRequireDTO, id);
         return ResponseEntity.ok(productResponseDTO);
       } catch (Exception e) {
         throw new ProductCreatingException(e.getMessage());
       }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
        @RequestParam Long id
        ) {
       try {
         ProductResponseDTO productResponseDTO = productService.deleteProductById(id);
         return ResponseEntity.ok(productResponseDTO);
       } catch (Exception e) {
         throw new ProductCreatingException(e.getMessage());
       }
    }
}
