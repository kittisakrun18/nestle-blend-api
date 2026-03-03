package com.nestle.blend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportEntryFailDto {
    private UUID id;
    private String jobId;
    private LocalDate insertDate;
    private String categoryName;
    private String sheetName;
    private Integer excelRowNo;
    private String seqNoRaw;
    private String fullName;
    private String email;
    private String zone;
    private String reward;
    private String purchasedAtRaw;
    private String reason;
}
