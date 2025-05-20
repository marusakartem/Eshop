package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.ProductDTO;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.entity.ProductEntity;
import com.university.coursework.exception.BrandNotFoundException;
import com.university.coursework.exception.ProductNotFoundException;
import com.university.coursework.repository.BrandRepository;
import com.university.coursework.repository.ProductRepository;
import com.university.coursework.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private BrandRepository brandRepository;

    private ProductServiceImpl productService;

    private UUID productId;
    private UUID brandId;
    private ProductEntity productEntity;
    private ProductDTO productDTO;
    private BrandEntity brandEntity;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, brandRepository);
        productId = UUID.randomUUID();
        brandId = UUID.randomUUID();

        brandEntity = BrandEntity.builder()
                .id(brandId)
                .name("Luxury Brand")
                .description("Premium products")
                .logoUrl("/brands/luxury.png")
                .build();

        productEntity = ProductEntity.builder()
                .id(productId)
                .name("Chanel No 5")
                .description("Classic perfume")
                .brand(brandEntity)
                .price(BigDecimal.valueOf(120.99))
                .stockQuantity(50)
                .createdAt(LocalDateTime.now())
                .build();

        productDTO = ProductDTO.builder()
                .name("Chanel No 5")
                .description("Classic perfume")
                .brandId(brandId)
                .price(BigDecimal.valueOf(120.99))
                .stockQuantity(50)
                .build();
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(productEntity));

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Chanel No 5", result.get(0).getName());
        verify(productRepository).findAll();
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));

        ProductDTO result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals("Chanel No 5", result.getName());
        assertEquals(brandId, result.getBrandId());
        verify(productRepository).findById(productId);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(productId));
    }

    @Test
    void testFindByBrandId() {
        when(productRepository.findByBrandId(brandId)).thenReturn(List.of(productEntity));

        List<ProductDTO> result = productService.findByBrandId(brandId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Chanel No 5", result.get(0).getName());
        verify(productRepository).findByBrandId(brandId);
    }

    @Test
    void testCreateProduct() {
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brandEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);

        ProductDTO result = productService.createProduct(productDTO);

        assertNotNull(result);
        assertEquals("Chanel No 5", result.getName());
        assertEquals(50, result.getStockQuantity());
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void testCreateProductBrandNotFound() {
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class,
                () -> productService.createProduct(productDTO));
    }

    @Test
    void testUpdateProduct() {
        ProductEntity existingProduct = ProductEntity.builder()
                .id(productId)
                .name("Chanel No 5 Eau de Parfum")
                .description("Updated version")
                .brand(brandEntity)
                .price(BigDecimal.valueOf(150.99))
                .stockQuantity(30)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brandEntity));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity saved = invocation.getArgument(0);
            return saved.toBuilder().id(productId).build();
        });

        ProductDTO updatedDTO = ProductDTO.builder()
                .name("Chanel No 5 Eau de Parfum")
                .description("Updated version")
                .brandId(brandId)
                .price(BigDecimal.valueOf(150.99))
                .stockQuantity(30)
                .build();

        ProductDTO result = productService.updateProduct(productId, updatedDTO);

        assertNotNull(result);
        assertEquals("Chanel No 5 Eau de Parfum", result.getName());
        assertEquals(BigDecimal.valueOf(150.99), result.getPrice());
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.existsById(productId)).thenReturn(true);

        productService.deleteProduct(productId);

        verify(productRepository).deleteById(productId);
    }

    @Test
    void testDeleteProductNotFound() {
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(productId));
    }
}