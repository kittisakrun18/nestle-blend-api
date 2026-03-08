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
public class ClaimNotSubmissionDto {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String seqNo;
    private String fullName;
    private String email;
    private String zone;
    private String reward;
    private LocalDate purchasedAt;
    private LocalDateTime emailSentAt;
    private LocalDateTime expireAt;
    private String purchasedAtStr;
    private String emailSentAtStr;
    private String expireAtStr;
}
