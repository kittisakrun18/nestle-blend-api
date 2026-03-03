package com.nestle.blend.api.controller;

import com.nestle.blend.api.dto.BaseResponseDto;
import com.nestle.blend.api.dto.ImportEntryDto;
import com.nestle.blend.api.dto.ImportEntryFailDto;
import com.nestle.blend.api.dto.PaginationRespDto;
import com.nestle.blend.api.dto.admin.ImportJobStatusRespDto;
import com.nestle.blend.api.dto.admin.ImportJobSubmitRespDto;
import com.nestle.blend.api.exception.CustomException;
import com.nestle.blend.api.service.ImportEntryFailService;
import com.nestle.blend.api.service.ImportEntryService;
import com.nestle.blend.api.service.job.ImportExcelJobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(value = "${app.api.path.prefix}/admin/import")
public class AdminImportCtrl extends ABaseCtrl {
    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private ImportExcelJobService importExcelJobService;
    @Autowired
    private ImportEntryFailService importEntryFailService;
    @Autowired
    private ImportEntryService importEntryService;

    public AdminImportCtrl(HttpServletRequest req,
                           HttpServletResponse res) {
        super(req, res);
    }

    @GetMapping(value = "/find-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<PaginationRespDto<ImportEntryDto>>> findAll(@RequestParam(value = "categoryId", required = false) UUID categoryId,
                                                                                          @RequestParam(value = "emailStatus", required = false) String emailStatus,
                                                                                          @RequestParam("page") int page,
                                                                                          @RequestParam("limit") int limit) {
        PaginationRespDto<ImportEntryDto> result = null;
        try {
            this.log.info("Request : {} {} {} {}", categoryId, emailStatus, page, limit);
            result = this.importEntryService.findAll(categoryId, emailStatus, page, limit);
            this.log.info("Result : {}", result);
        } catch (CustomException e) {
            e.printStackTrace();
            this.log.error(e);
            return new ResponseEntity<>(super.responseError(e.getCode(), e.getMessage(), null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            this.log.error(e);
            return ResponseEntity.internalServerError().body(super.responseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }

        return ResponseEntity.ok(super.responseSuccess(result));
    }

    @GetMapping(value = "/find-all-fail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<PaginationRespDto<ImportEntryFailDto>>> findAll(@RequestParam(value = "startDate", required = false) LocalDate startDate,
                                                                                          @RequestParam(value = "endDate", required = false) LocalDate endDate,
                                                                                          @RequestParam("page") int page,
                                                                                          @RequestParam("limit") int limit) {
        PaginationRespDto<ImportEntryFailDto> result = null;
        try {
            this.log.info("Request : {} {} {} {}", startDate, endDate, page, limit);
            result = this.importEntryFailService.findAll(startDate, endDate, page, limit);
            this.log.info("Result : {}", result);
        } catch (CustomException e) {
            e.printStackTrace();
            this.log.error(e);
            return new ResponseEntity<>(super.responseError(e.getCode(), e.getMessage(), null), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            this.log.error(e);
            return ResponseEntity.internalServerError().body(super.responseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }

        return ResponseEntity.ok(super.responseSuccess(result));
    }

    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<ImportJobSubmitRespDto>> uploadExcel(@RequestPart("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(responseError(400, "File is required", null));
        }

        UUID adminUserId = getAdminUserId();

        // Save to temp file for async processing
        Path tmp = Files.createTempFile("nestle-import-", "-" + file.getOriginalFilename());
        file.transferTo(tmp.toFile());

        String jobId = importExcelJobService.submitJob(tmp, adminUserId);
        return ResponseEntity.accepted().body(responseSuccess(new ImportJobSubmitRespDto(jobId)));
    }

    @GetMapping(value = "/jobs/{jobId}")
    public ResponseEntity<BaseResponseDto<ImportJobStatusRespDto>> getJobStatus(@PathVariable("jobId") String jobId) {
        ImportJobStatusRespDto st = importExcelJobService.getStatus(jobId);
        if (st == null) {
            return ResponseEntity.status(404).body(responseError(404, "Job not found", null));
        }
        return ResponseEntity.ok(responseSuccess(st));
    }

    private UUID getAdminUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return null;
            Object principal = auth.getPrincipal();
            if (principal == null) return null;
            return UUID.fromString(String.valueOf(principal));
        } catch (Exception e) {
            return null;
        }
    }
}
