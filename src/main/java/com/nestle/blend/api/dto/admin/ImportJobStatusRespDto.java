package com.nestle.blend.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJobStatusRespDto {
    private String jobId;
    private String status;          // QUEUED/RUNNING/SUCCESS/FAILED
    private int totalSheets;
    private int totalRows;
    private int insertedRows;
    private int skippedRows;
    private int failedRows;         // rows saved into import_entry_fail
    private String errorMessage;
}
