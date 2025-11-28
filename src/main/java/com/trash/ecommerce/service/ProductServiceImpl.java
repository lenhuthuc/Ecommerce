package com.trash.ecommerce.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
        productDTO.setPrice(product.getPrice());
        productDTO.setProduct_name(product.getProductName());
        productDTO.setQuantity(product.getQuantity());
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
        Product product = new Product();
        String fileResource = UUID.randomUUID() + "_" + file.getOriginalFilename();
        product.setImgName(file.getOriginalFilename());
        Path path = Paths.get("uploads/" + fileResource);
        Files.copy(file.getInputStream(),path);
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
        if (!file.isEmpty()) {
            String oldImgPath = product.getImgData();
            if (oldImgPath != null) {
                File oldFile = new File(oldImgPath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get("uploads/" + filename);
            Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

            product.setImgName(file.getOriginalFilename());
            product.setImgData(uploadPath.toString());
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
        for(CartItem cartItem : product.getCartItems())  {
            cartItem.setProduct(null);
        }
        productRepository.delete(product);
        return new ProductResponseDTO("successful");
    }

    @Override
    public ProductResponseDTO addToCart(String token, Long productId, Long quantity) {
        Long userId = jwtService.extractId(token);
        Users users = userRepository.findById(userId)
                                        .orElseThrow(() -> new FindingUserError("user is not found"));
        Cart cart = users.getCart();
        Long cartId = cart.getId();
        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new ProductFingdingException("product can't be found"));
        CartItemId cartItemId = new CartItemId(cartId, productId);
        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity); 
        product.getCartItems().add(cartItem);
        cart.getItems().add(cartItem);
        return new ProductResponseDTO("Them san pham vao gio hang thanh cong !");
    }

    @Override
    public String getImgProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductFingdingException("Product not found"));
        return product.getImgData();
    }

}
