package com.nestle.blend.api.dto.claim;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitClaimRespDto {
    private UUID claimId;
}
