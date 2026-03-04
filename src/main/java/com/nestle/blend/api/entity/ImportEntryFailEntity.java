package com.nestle.blend.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "import_entry_fail")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportEntryFailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_id", nullable = false, length = 80)
    private String jobId;

    @Column(name = "insert_date", nullable = false)
    private LocalDate insertDate;

    @Column(name = "category_name", length = 255)
    private String categoryName;

    @Column(name = "sheet_name", length = 255)
    private String sheetName;

    @Column(name = "excel_row_no")
    private Integer excelRowNo;

    @Column(name = "seq_no_raw", length = 50)
    private String seqNoRaw;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "zone", length = 100)
    private String zone;

    @Column(name = "reward", length = 255)
    private String reward;

    @Column(name = "purchased_at_raw", length = 50)
    private String purchasedAtRaw;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
