package com.trash.ecommerce.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.trash.ecommerce.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.trash.ecommerce.dto.ProductDetailsResponseDTO;
import com.trash.ecommerce.dto.ProductRequestDTO;
import com.trash.ecommerce.dto.ProductResponseDTO;
import com.trash.ecommerce.entity.Cart;
import com.trash.ecommerce.entity.CartItem;
import com.trash.ecommerce.entity.CartItemId;
import com.trash.ecommerce.entity.Product;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.ProductFingdingException;
import com.trash.ecommerce.repository.ProductRepository;
import com.trash.ecommerce.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductMapper productMapper;
    @Override
    public ProductDetailsResponseDTO findProductById(Long id) {
        ProductDetailsResponseDTO productDTO = new ProductDetailsResponseDTO();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductFingdingException("Không tìm thấy sản phẩm"));
        productDTO = productMapper.mapperProduct(product);
        return productDTO;
    }

    @Override
    public List<ProductDetailsResponseDTO> findAllProduct(int noPage, int sizePage) {
        PageRequest pageRequest = PageRequest.of(noPage, sizePage);
        Page<Product> products = productRepository.findAll(pageRequest);
        List<ProductDetailsResponseDTO> productsDTOs = products
                .getContent()
                .stream()
                .map(product -> productMapper.mapperProduct(product))
                .toList();
        return productsDTOs;
    }

    @Override
    public List<ProductDetailsResponseDTO> findProductByName(String name, int noPage, int sizePage) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        PageRequest pageRequest = PageRequest.of(noPage, sizePage);
        Page<Product> products = productRepository.findProductsByName(name, pageRequest);
        List<ProductDetailsResponseDTO> productDetailsResponseDTO = products.getContent()
                                                                                .stream()
                                                                                .map(product -> productMapper.mapperProduct(product))
                                                                                .toList();
        return productDetailsResponseDTO;
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        Product product = new Product();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        
        String fileResource = UUID.randomUUID() + "_" + originalFilename;
        product.setImgName(originalFilename);
        Path path = Paths.get("uploads/" + fileResource);
        Files.copy(file.getInputStream(), path);
        product.setImgData(fileResource);
        product.setPrice(productRequestDTO.getPrice());
        product.setProductName(productRequestDTO.getProductName());
        product.setQuantity(productRequestDTO.getQuantity());
        productRepository.save(product);
        return new ProductResponseDTO("creating product is successful");
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTO, Long id, MultipartFile file) throws IOException {
        Product product = productRepository.findById(id).orElseThrow(
            () -> new ProductFingdingException("Product is not found")
        );
        if (file != null && !file.isEmpty()) {
            String oldImgPath = product.getImgData();
            if (oldImgPath != null && !oldImgPath.isEmpty()) {
                Path oldFilePath = Paths.get("uploads/" + oldImgPath);
                File oldFile = oldFilePath.toFile();
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("File name is required");
            }
            
            String filename = UUID.randomUUID() + "_" + originalFilename;

            Path uploadPath = Paths.get("uploads/" + filename);
            Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

            product.setImgName(originalFilename);
            product.setImgData(filename);
        }
        if (productRequestDTO.getPrice() != null) {
            product.setPrice(productRequestDTO.getPrice());
        }
        if (productRequestDTO.getProductName() != null && !productRequestDTO.getProductName().isEmpty()) {
            product.setProductName(productRequestDTO.getProductName());
        }
        if (productRequestDTO.getQuantity() != null) {
            product.setQuantity(productRequestDTO.getQuantity());
        }
        productRepository.save(product);
        return new ProductResponseDTO("Update product is successful");
    }

    @Override
    @Transactional
    public ProductResponseDTO deleteProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
            () -> new ProductFingdingException("Product is not found")
        );
        if (product.getCartItems() != null) {
            for(CartItem cartItem : product.getCartItems())  {
                if (cartItem != null) {
                    cartItem.setProduct(null);
                }
            }
        }
        productRepository.delete(product);
        return new ProductResponseDTO("successful");
    }

    @Override
    public ProductResponseDTO addToCart(String token, Long productId, Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        Long userId = jwtService.extractId(token);
        Users users = userRepository.findById(userId)
                                        .orElseThrow(() -> new FindingUserError("user is not found"));
        Cart cart = users.getCart();
        if (cart == null) {
            throw new FindingUserError("Cart not found for user");
        }
        
        Long cartId = cart.getId();
        if (cartId == null) {
            throw new FindingUserError("Cart ID is null");
        }
        
        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new ProductFingdingException("product can't be found"));
        CartItemId cartItemId = new CartItemId(cartId, productId);
        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        
        if (product.getCartItems() == null) {
            product.setCartItems(new HashSet<>());
        }
        product.getCartItems().add(cartItem);
        
        if (cart.getItems() == null) {
            cart.setItems(new HashSet<>());
        }
        cart.getItems().add(cartItem);
        return new ProductResponseDTO("Them san pham vao gio hang thanh cong !");
    }

    @Override
    public String getImgProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductFingdingException("Product not found"));
        String imgData = product.getImgData();
        if (imgData == null || imgData.isEmpty()) {
            throw new ProductFingdingException("Product image not found");
        }
        return imgData;
    }

}
