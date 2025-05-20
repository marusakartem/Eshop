package com.university.coursework.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.university.coursework.domain.BrandDTO;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.exception.BrandNotFoundException;
import com.university.coursework.repository.BrandRepository;
import com.university.coursework.service.impl.BrandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    private BrandServiceImpl brandService;

    private UUID brandId;
    private BrandEntity brandEntity;
    private BrandDTO brandDTO;

    @BeforeEach
    void setUp() {
        brandService = new BrandServiceImpl(brandRepository);
        brandId = UUID.randomUUID();

        brandEntity = BrandEntity.builder()
                .id(brandId)
                .name("Luxury Brands")
                .description("Premium quality products")
                .logoUrl("/logos/luxury.png")
                .build();

        brandDTO = BrandDTO.builder()
                .id(brandId)
                .name("Luxury Brands")
                .description("Premium quality products")
                .logoUrl("/logos/luxury.png")
                .build();
    }

    @Test
    void testFindAllBrands() {
        when(brandRepository.findAll()).thenReturn(List.of(brandEntity));

        List<BrandDTO> result = brandService.findAllBrands();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Luxury Brands", result.get(0).getName());
        verify(brandRepository).findAll();
    }

    @Test
    void testFindBrandById() {
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brandEntity));

        BrandDTO result = brandService.findBrandById(brandId);

        assertNotNull(result);
        assertEquals(brandId, result.getId());
        assertEquals("Premium quality products", result.getDescription());
        verify(brandRepository).findById(brandId);
    }

    @Test
    void testFindBrandByIdNotFound() {
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());
        assertThrows(BrandNotFoundException.class, () -> brandService.findBrandById(brandId));
    }

    @Test
    void testCreateBrand() {
        when(brandRepository.save(any(BrandEntity.class))).thenReturn(brandEntity);

        BrandDTO result = brandService.createBrand(brandDTO);

        assertNotNull(result);
        assertEquals("Luxury Brands", result.getName());
        verify(brandRepository).save(any(BrandEntity.class));
    }

    @Test
    void testUpdateBrand() {
        BrandEntity existingBrand = BrandEntity.builder()
                .id(brandId)
                .name("Old Name")
                .description("Old Description")
                .logoUrl("/old-logo.png")
                .build();

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.save(any(BrandEntity.class))).thenReturn(brandEntity);

        BrandDTO updatedDTO = BrandDTO.builder()
                .id(brandId)
                .name("Updated Name")
                .description("New Description")
                .logoUrl("/new-logo.png")
                .build();

        BrandDTO result = brandService.updateBrand(brandId, updatedDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("New Description", result.getDescription());
        verify(brandRepository).save(any(BrandEntity.class));
    }

    @Test
    void testDeleteBrand() {
        when(brandRepository.existsById(brandId)).thenReturn(true);
        brandService.deleteBrand(brandId);
        verify(brandRepository).deleteById(brandId);
    }

    @Test
    void testDeleteBrandNotFound() {
        when(brandRepository.existsById(brandId)).thenReturn(false);
        assertThrows(BrandNotFoundException.class, () -> brandService.deleteBrand(brandId));
    }
}