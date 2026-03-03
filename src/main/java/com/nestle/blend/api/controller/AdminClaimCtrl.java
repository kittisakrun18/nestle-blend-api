package com.nestle.blend.api.controller;

import com.nestle.blend.api.dto.BaseResponseDto;
import com.nestle.blend.api.dto.ClaimSubmissionDto;
import com.nestle.blend.api.dto.PaginationRespDto;
import com.nestle.blend.api.exception.CustomException;
import com.nestle.blend.api.service.ClaimSubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(value = "${app.api.path.prefix}/admin/claims")
public class AdminClaimCtrl extends ABaseCtrl {
    private Logger log = LogManager.getLogger(this.getClass());

    @Autowired
    private ClaimSubmissionService claimSubmissionService;

    public AdminClaimCtrl(HttpServletRequest req,
                          HttpServletResponse res) {
        super(req, res);
    }

    @GetMapping(value = "/find-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<PaginationRespDto<ClaimSubmissionDto>>> findAll(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) UUID categoryId,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate,
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        PaginationRespDto<ClaimSubmissionDto> result = null;
        try {
            this.log.info("Request : {} {} {} {} {} {}", search, categoryId, startDate, endDate, page, limit);
            result = this.claimSubmissionService.findAll(search, categoryId, startDate, endDate, page, limit);
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

    @GetMapping(value = "/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) UUID categoryId,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {
        try {
            this.log.info("Request : {} {} {} {}", search, categoryId, startDate, endDate);
            byte[] results = this.claimSubmissionService.export(search, categoryId, startDate, endDate);
            String filename = "ฟอร์มยืนยันบันทึกข้อมูล.xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8))
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(results);
        } catch (Exception e) {
            e.printStackTrace();
            this.log.error(e);
            return ResponseEntity.status(500).body(new byte[0]);
        }
    }
}
