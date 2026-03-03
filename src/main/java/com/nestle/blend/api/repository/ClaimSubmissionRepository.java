package com.nestle.blend.api.repository;

import com.nestle.blend.api.entity.ClaimSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimSubmissionRepository extends JpaRepository<ClaimSubmissionEntity, UUID> {
    Optional<ClaimSubmissionEntity> findByImportEntry_Id(UUID importEntryId);
}
