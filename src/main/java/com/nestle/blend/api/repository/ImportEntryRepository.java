package com.nestle.blend.api.repository;

import com.nestle.blend.api.entity.ImportEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImportEntryRepository extends JpaRepository<ImportEntryEntity, UUID> {
    Optional<ImportEntryEntity> findByEmailIgnoreCase(String email);
    Optional<ImportEntryEntity> findByTokenHash(String tokenHash);
}
