package com.trash.ecommerce.mapper;

import com.trash.ecommerce.dto.ProductDetailsResponseDTO;
import com.trash.ecommerce.dto.ReviewResponse;
import com.trash.ecommerce.entity.Product;
import com.trash.ecommerce.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {
    @Autowired
    private ReviewsMapper mapper;
    public ProductDetailsResponseDTO mapperProduct(Product product) {
        ProductDetailsResponseDTO productDTO = new ProductDetailsResponseDTO();
        productDTO.setProduct_name(product.getProductName());
        productDTO.setQuantity(product.getQuantity());
        productDTO.setPrice(product.getPrice());
        return productDTO;
    }
}
