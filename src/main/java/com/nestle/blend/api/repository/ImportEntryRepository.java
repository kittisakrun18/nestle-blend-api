package com.nestle.blend.api.repository;

import com.nestle.blend.api.entity.ImportEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImportEntryRepository extends JpaRepository<ImportEntryEntity, UUID> {
    Optional<ImportEntryEntity> findByEmailIgnoreCase(String email);
    Optional<ImportEntryEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ImportEntryEntity e where e.id = :id")
    Optional<ImportEntryEntity> findByIdForUpdate(@Param("id") UUID id);

    Page<ImportEntryEntity> findByEmailStatusInAndUsedAtIsNull(List<String> emailStatus, Pageable pageable);
    ImportEntryEntity findFirstById(@Param("id") UUID id);
}
