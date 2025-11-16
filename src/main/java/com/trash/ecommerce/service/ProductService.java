package com.trash.ecommerce.service;

import java.util.List;

import com.trash.ecommerce.dto.ProductDetailsResponseDTO;
import com.trash.ecommerce.dto.ProductRequireDTO;
import com.trash.ecommerce.dto.ProductResponseDTO;

public interface ProductService {
    public ProductDetailsResponseDTO findProductById(Long id);
    public List<ProductDetailsResponseDTO> findAllProduct(int noPage, int sizePage);
    public List<ProductDetailsResponseDTO> findProductByName(String name, int noPage, int sizePage);
    public ProductResponseDTO createProduct(ProductRequireDTO productRequireDTO);
    public ProductResponseDTO updateProduct(ProductRequireDTO productRequireDTO, Long id);
    public ProductResponseDTO deleteProductById(Long id);
    public ProductResponseDTO addToCart(String token, Long productId);
}
