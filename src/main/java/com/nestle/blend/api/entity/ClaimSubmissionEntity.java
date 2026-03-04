package com.nestle.blend.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "claim_submission")
public class ClaimSubmissionEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_entry_id", nullable = false, unique = true)
    private ImportEntryEntity importEntry;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "age_u20")
    private String ageU20;

    @Column(name = "id_card_file_path", nullable = false)
    private String idCardFilePath;

    @Column(name = "receipt_file_path", nullable = false)
    private String receiptFilePath;

    @Column(name = "parent_file_path", nullable = false)
    private String parentFilePath;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
