package com.trash.ecommerce.service;

import java.io.IOException;
import java.util.List;

import com.trash.ecommerce.dto.ProductDetailsResponseDTO;
import com.trash.ecommerce.dto.ProductRequestDTO;
import com.trash.ecommerce.dto.ProductResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    public ProductDetailsResponseDTO findProductById(Long id);
    public List<ProductDetailsResponseDTO> findAllProduct(int noPage, int sizePage);
    public List<ProductDetailsResponseDTO> findProductByName(String name, int noPage, int sizePage);
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, MultipartFile file) throws IOException;
    public ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTO, Long id, MultipartFile file) throws IOException;
    public ProductResponseDTO deleteProductById(Long id);
    public ProductResponseDTO addToCart(String token, Long productId, Long quantity);
    public String getImgProduct(Long productId);
}
