package com.nestle.blend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSubmissionDto {
    private UUID id;
    private UUID importEntryId;
    private String categoryName;
    private Integer seqNo;
    private String fullName;
    private String email;
    private String phone;
    private String ageU20;
    private String idCardFilePath;
    private String receiptFilePath;
    private LocalDateTime submittedAt;
}
