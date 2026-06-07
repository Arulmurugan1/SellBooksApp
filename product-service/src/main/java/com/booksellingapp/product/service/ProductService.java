package com.booksellingapp.product.service;

import com.booksellingapp.product.dto.ProductDTO;
import com.booksellingapp.product.entity.Product;
import com.booksellingapp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Get product by product code
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductByCode(String productCode) {
        log.info("Fetching product with code: {}", productCode);
        return productRepository.findByProductCode(productCode)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Product not found with code: " + productCode));
    }

    /**
     * Create new product
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getProductCode());
        
        if (productRepository.existsByIsbn(productDTO.getIsbn())) {
            throw new RuntimeException("Product with ISBN: " + productDTO.getIsbn() + " already exists");
        }

        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return convertToDTO(savedProduct);
    }

    /**
     * Update product
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setTitle(productDTO.getTitle());
        product.setDescription(productDTO.getDescription());
        product.setAuthor(productDTO.getAuthor());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        product.setAvailable(productDTO.getAvailable());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {}", id);
        return convertToDTO(updatedProduct);
    }

    /**
     * Delete product
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

    /**
     * Check if product exists and is available
     */
    @Transactional(readOnly = true)
    public boolean isProductAvailable(String productCode) {
        log.debug("Checking availability for product code: {}", productCode);
        return productRepository.findByProductCode(productCode)
                .map(Product::getAvailable)
                .orElse(false);
    }

    /**
     * Convert Product entity to ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .title(product.getTitle())
                .description(product.getDescription())
                .author(product.getAuthor())
                .isbn(product.getIsbn())
                .price(product.getPrice())
                .category(product.getCategory())
                .available(product.getAvailable())
                .build();
    }

    /**
     * Convert ProductDTO to Product entity
     */
    private Product convertToEntity(ProductDTO productDTO) {
        return Product.builder()
                .productCode(productDTO.getProductCode())
                .title(productDTO.getTitle())
                .description(productDTO.getDescription())
                .author(productDTO.getAuthor())
                .isbn(productDTO.getIsbn())
                .price(productDTO.getPrice())
                .category(productDTO.getCategory())
                .available(productDTO.getAvailable())
                .build();
    }
}
