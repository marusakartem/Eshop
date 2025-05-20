package com.university.coursework.service;

import com.university.coursework.domain.ProductDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(UUID id);
    List<ProductDTO> searchProducts(String name, String brand, BigDecimal minPrice, BigDecimal maxPrice);
    List<ProductDTO> findByBrandId(UUID categoryId);
    List<ProductDTO> findAll();
    ProductDTO findById(UUID id);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(UUID id, ProductDTO productDTO);
    void deleteProduct(UUID id);
}