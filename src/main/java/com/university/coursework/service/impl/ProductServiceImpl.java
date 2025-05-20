package com.university.coursework.service.impl;

import com.university.coursework.domain.ProductDTO;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.exception.BrandNotFoundException;
import com.university.coursework.exception.ProductNotFoundException;
import com.university.coursework.repository.BrandRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return mapToDto(product);
    }

    @Override
    public List<ProductDTO> searchProducts(String name, String brand, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByFilters(name, brand, minPrice, maxPrice)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<ProductDTO> findByBrandId(UUID categoryId) {
        return productRepository.findByBrandId(categoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO findById(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return mapToDto(product);
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        BrandEntity brand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + productDTO.getBrandId()));

        ProductEntity product = mapToEntity(productDTO).toBuilder()
                .brand(brand)
                .build();

        ProductEntity savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(UUID id, ProductDTO productDTO) {
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        BrandEntity brand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(() -> new ProductNotFoundException("Category not found with id: " + productDTO.getBrandId()));

        ProductEntity updatedProduct = existingProduct.toBuilder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .brand(brand)
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .build();

        ProductEntity savedProduct = productRepository.save(updatedProduct);
        return mapToDto(savedProduct);
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductDTO mapToDto(ProductEntity entity) {
        return ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .brandId(entity.getBrand().getId())
                .stockQuantity(entity.getStockQuantity())
                .build();
    }

    private ProductEntity mapToEntity(ProductDTO dto) {
        return ProductEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .build();
    }
}