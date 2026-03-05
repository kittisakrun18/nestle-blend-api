package com.nestle.blend.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "import_entry",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_import_entry_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uq_import_entry_token_hash", columnNames = {"token_hash"}),
                @UniqueConstraint(name = "uq_import_entry_category_seqno", columnNames = {"category_id", "seq_no"})
        }
)
public class ImportEntryEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "seq_no")
    private String seqNo;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "zone")
    private String zone;

    @Column(name = "reward")
    private String reward;

    @Column(name = "purchased_at")
    private LocalDate purchasedAt;

    @Builder.Default
    @Column(name = "email_status", nullable = false)
    private String emailStatus = "PENDING";

    @Column(name = "email_error")
    private String emailError;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AdminUserEntity createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
