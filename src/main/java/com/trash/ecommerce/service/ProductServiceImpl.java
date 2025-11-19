package com.trash.ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.trash.ecommerce.dto.ProductDetailsResponseDTO;
import com.trash.ecommerce.dto.ProductRequireDTO;
import com.trash.ecommerce.dto.ProductResponseDTO;
import com.trash.ecommerce.entity.Cart;
import com.trash.ecommerce.entity.CartItem;
import com.trash.ecommerce.entity.CartItemId;
import com.trash.ecommerce.entity.InvoiceItem;
import com.trash.ecommerce.entity.Product;
import com.trash.ecommerce.entity.Review;
import com.trash.ecommerce.entity.Users;
import com.trash.ecommerce.exception.FindingUserError;
import com.trash.ecommerce.exception.ProductFingdingException;
import com.trash.ecommerce.repository.ProductRepository;
import com.trash.ecommerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Override
    public ProductDetailsResponseDTO findProductById(Long id) {
        ProductDetailsResponseDTO productDTO = new ProductDetailsResponseDTO();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException());
        productDTO.setImg(product.getImg());
        productDTO.setPrice(product.getPrice());
        productDTO.setProduct_name(product.getProductName());
        productDTO.setQuantity(product.getQuantity());
        return productDTO;
    }

    @Override
    public List<ProductDetailsResponseDTO> findAllProduct(int noPage, int sizePage) {
        PageRequest pageRequest = PageRequest.of(noPage, sizePage);
        Page<Product> products = productRepository.findAll(pageRequest);
        List<ProductDetailsResponseDTO> productsDTO = products
                .getContent()
                .stream()
                .map(
                        product -> new ProductDetailsResponseDTO(
                                product.getImg(),
                                product.getProductName(),
                                product.getPrice(),
                                product.getQuantity()))
                .toList();
        return productsDTO;
    }

    @Override
    public List<ProductDetailsResponseDTO> findProductByName(String name, int noPage, int sizePage) {
        PageRequest pageRequest = PageRequest.of(noPage, sizePage);
        Page<Product> products = productRepository.findProductsByName(name, pageRequest);
        List<ProductDetailsResponseDTO> productDetailsResponseDTO = products.getContent()
                                                                                .stream()
                                                                                .map(
                                                                                    product -> new ProductDetailsResponseDTO(
                                                                                        product.getImg(),
                                                                                        product.getProductName(),
                                                                                        product.getPrice(),
                                                                                        product.getQuantity()
                                                                                    )
                                                                                ).toList();
        return productDetailsResponseDTO;
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequireDTO productRequireDTO) {
        Product product = new Product();
        product.setImg(productRequireDTO.getImg());
        product.setPrice(productRequireDTO.getPrice());
        product.setProductName(productRequireDTO.getProductName());
        product.setQuantity(productRequireDTO.getQuantity());
        productRepository.save(product);
        return new ProductResponseDTO("creating product is successful");
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(ProductRequireDTO productRequireDTO, Long id) {
        Product product = productRepository.findById(id).orElseThrow(
            () -> new ProductFingdingException("Product is not found")
        );
        if (productRequireDTO.getImg() != null && !productRequireDTO.getImg().isEmpty()) {
            product.setImg(productRequireDTO.getImg());
        }
        if (productRequireDTO.getPrice() != null) {
            product.setPrice(productRequireDTO.getPrice());
        }
        if (productRequireDTO.getProductName() != null && !productRequireDTO.getProductName().isEmpty()) {
            product.setProductName(productRequireDTO.getProductName());
        }
        if (productRequireDTO.getQuantity() != null) {
            product.setQuantity(productRequireDTO.getQuantity());
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
        cart.getItems().add(cartItem);
        cartItem.setProduct(product);
        product.getCartItems().add(cartItem);
        cartItem.setQuantity(quantity);
        return new ProductResponseDTO("Them san pham vao gio hang thanh cong !");
    }

}
