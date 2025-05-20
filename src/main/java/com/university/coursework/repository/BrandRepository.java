package com.university.coursework.repository;

import com.university.coursework.domain.BrandDTO;
import com.university.coursework.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, UUID> {
    BrandEntity findByName(String name);
}