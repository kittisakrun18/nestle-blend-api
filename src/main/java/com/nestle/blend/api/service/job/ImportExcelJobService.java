package com.nestle.blend.api.service.job;

import com.nestle.blend.api.dto.admin.ImportJobStatusRespDto;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImportExcelJobService {

    // job status in-memory (minimal)
    private final Map<String, ImportJobStatusRespDto> jobs = new ConcurrentHashMap<>();

    private final ImportExcelJobRunner runner;

    public ImportExcelJobService(ImportExcelJobRunner runner) {
        this.runner = runner;
    }

    public String submitJob(Path uploadedFilePath, UUID adminUserId) {
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, ImportJobStatusRespDto.builder()
                .jobId(jobId)
                .status("QUEUED")
                .totalSheets(0)
                .totalRows(0)
                .insertedRows(0)
                .skippedRows(0)
                .failedRows(0)
                .build());

        // IMPORTANT: call async method on a DIFFERENT Spring bean (proxy applies)
        runner.runJobAsync(jobId, uploadedFilePath, adminUserId, jobs);

        return jobId;
    }

    public ImportJobStatusRespDto getStatus(String jobId) {
        return jobs.get(jobId);
    }
}
