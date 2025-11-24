package com.trash.ecommerce.controller;

import jakarta.annotation.Resource;
import jdk.jfr.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.trash.ecommerce.dto.ProductDetailsResponseDTO;
import com.trash.ecommerce.dto.ProductRequireDTO;
import com.trash.ecommerce.dto.ProductResponseDTO;
import com.trash.ecommerce.exception.ProductCreatingException;
import com.trash.ecommerce.exception.ProductFingdingException;
import com.trash.ecommerce.service.ProductService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/product")
public class ProductController {
    private Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private ProductService productService;
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsResponseDTO> findProductById(
        @PathVariable Long id
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
    
    @GetMapping("/products")
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
    
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
        @PathVariable Long id
        ) {
       try {
         ProductResponseDTO productResponseDTO = productService.deleteProductById(id);
         return ResponseEntity.ok(productResponseDTO);
       } catch (Exception e) {
         throw new ProductCreatingException(e.getMessage());
       }
    }

    @GetMapping("/products/{id}/img")
    public ResponseEntity<?> getProductImg(
            @PathVariable long id
    ) throws IOException {
        try {
            Path path = Paths.get(productService.getImgProduct(id));
            UrlResource resource = new UrlResource(path.toUri());
            String contentType = "application/octet-stream";
            if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (path.endsWith(".gif")) contentType = "image/gif";

            return ResponseEntity.ok().contentType(MediaType.valueOf(contentType)).body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
