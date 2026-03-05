package com.nestle.blend.api.dto.claim;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenRespDto {
    private UUID importEntryId;
    private String email;
    private String categoryName;
    private String seqNo;
    private LocalDateTime expiresAt;
}
