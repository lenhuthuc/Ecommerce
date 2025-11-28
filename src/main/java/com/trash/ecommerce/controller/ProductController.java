package com.trash.ecommerce.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.trash.ecommerce.dto.ProductDetailsResponseDTO;
import com.trash.ecommerce.exception.ProductFingdingException;
import com.trash.ecommerce.service.ProductService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/products")
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
        } catch (ProductFingdingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/")
    public ResponseEntity<List<ProductDetailsResponseDTO>> getAllProduct(
        @RequestParam(value = "noPage", defaultValue = "0") int noPage,
        @RequestParam(value = "sizePage", defaultValue = "30") int sizePage
    ) {
        try {
            List<ProductDetailsResponseDTO> products = productService.findAllProduct(noPage, sizePage);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{id}/img")
    public ResponseEntity<?> getProductImg(
            @PathVariable long id
    ) throws IOException {
        try {
            Path path = Paths.get("uploads/" + productService.getImgProduct(id));
            UrlResource resource = new UrlResource(path.toUri());
            File file = path.toFile();
            String fileName = file.getName();
            String contentType = "application/octet-stream";
            if (fileName.endsWith(".png")) contentType = "image/png";
            else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fileName.endsWith(".gif")) contentType = "image/gif";

            return ResponseEntity.ok().contentType(MediaType.valueOf(contentType)).body(resource);
        } catch (ProductFingdingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
