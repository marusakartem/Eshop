package com.university.coursework.service;

import com.university.coursework.domain.BrandDTO;
import java.util.List;
import java.util.UUID;

public interface BrandService {
    List<BrandDTO> findAllBrands();
    BrandDTO findBrandById(UUID id);
    BrandDTO createBrand(BrandDTO brandDTO);
    BrandDTO updateBrand(UUID id, BrandDTO brandDTO);
    void deleteBrand(UUID id);
}