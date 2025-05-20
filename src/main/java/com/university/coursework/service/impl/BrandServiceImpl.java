package com.university.coursework.service.impl;

import com.university.coursework.domain.BrandDTO;
import com.university.coursework.entity.BrandEntity;
import com.university.coursework.exception.BrandNotFoundException;
import com.university.coursework.repository.BrandRepository;
import com.university.coursework.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public List<BrandDTO> findAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BrandDTO findBrandById(UUID id) {
        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + id));
        return mapToDto(brand);
    }

    @Override
    public BrandDTO createBrand(BrandDTO brandDTO) {
        BrandEntity brand = mapToEntity(brandDTO);
        BrandEntity savedBrand = brandRepository.save(brand);
        return mapToDto(savedBrand);
    }

    @Override
    public BrandDTO updateBrand(UUID id, BrandDTO brandDTO) {
        BrandEntity existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + id));

        BrandEntity updatedBrand = BrandEntity.builder()
                .id(existingBrand.getId())
                .name(brandDTO.getName())
                .description(brandDTO.getDescription())
                .logoUrl(brandDTO.getLogoUrl())
                .build();

        brandRepository.save(updatedBrand);
        return mapToDto(updatedBrand);
    }

    @Override
    public void deleteBrand(UUID id) {
        if (!brandRepository.existsById(id)) {
            throw new BrandNotFoundException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }

    private BrandDTO mapToDto(BrandEntity entity) {
        return BrandDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .logoUrl(entity.getLogoUrl())
                .build();
    }

    private BrandEntity mapToEntity(BrandDTO dto) {
        return BrandEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .logoUrl(dto.getLogoUrl())
                .build();
    }
}