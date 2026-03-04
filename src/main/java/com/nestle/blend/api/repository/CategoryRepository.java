package com.nestle.blend.api.repository;

import com.nestle.blend.api.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
    CategoryEntity findFirstById(UUID id);
}
