package com.nestle.blend.api.repository;

import com.nestle.blend.api.entity.ImportEntryFailEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface ImportEntryFailRepository extends JpaRepository<ImportEntryFailEntity, UUID> {

    Page<ImportEntryFailEntity> findByJobIdOrderByExcelRowNoAsc(String jobId, Pageable pageable);

    Page<ImportEntryFailEntity> findByInsertDateOrderByCreatedAtDesc(LocalDate insertDate, Pageable pageable);
}
