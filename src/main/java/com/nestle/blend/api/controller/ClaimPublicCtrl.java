package com.nestle.blend.api.controller;

import com.nestle.blend.api.dto.BaseResponseDto;
import com.nestle.blend.api.dto.claim.SubmitClaimRespDto;
import com.nestle.blend.api.dto.claim.ValidateTokenRespDto;
import com.nestle.blend.api.exception.CustomException;
import com.nestle.blend.api.service.ClaimPublicService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "${app.api.path.prefix}/claim")
public class ClaimPublicCtrl extends ABaseCtrl {

    private Logger log = LogManager.getLogger(this.getClass());

    private final ClaimPublicService claimPublicService;

    public ClaimPublicCtrl(HttpServletRequest req, HttpServletResponse res, ClaimPublicService claimPublicService) {
        super(req, res);
        this.claimPublicService = claimPublicService;
    }

    /**
     * Validate token (สำหรับ frontend ก่อนเข้า form)
     * GET /api/v1/claim/validate?token=...
     */
    @GetMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<ValidateTokenRespDto>> validate(@RequestParam("token") String token) {
        try {
            ValidateTokenRespDto data = claimPublicService.validateToken(token);
            return new ResponseEntity<>(this.responseSuccess(data), HttpStatus.OK);
        } catch (CustomException e) {
            return new ResponseEntity<>(this.responseError(e.getCode(), e.getMessage(), null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            this.log.error("validate token error", e);
            return new ResponseEntity<>(this.responseError(500, "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Submit form (multipart/form-data)
     * POST /api/v1/claim/submit
     *
     * Fields:
     * - token
     * - fullName
     * - phone
     * - ageU20 (true/false)
     * - idCardFile
     * - receiptFile
     * - parentIdCardFile (optional, required if ageU20=true)
     *
     * Note: รองรับชื่อไฟล์ parent แบบ frontend เดิม: guardianIdCardFile
     */
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<SubmitClaimRespDto>> submit(
            @RequestParam("token") String token,
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam(value = "ageU20", required = false, defaultValue = "false") boolean ageU20,
            @RequestPart(value = "idCardFile", required = false) MultipartFile idCardFile,
            @RequestPart(value = "receiptFile", required = false) MultipartFile receiptFile,
            @RequestPart(value = "parentFile", required = false) MultipartFile parentFile
    ) {
        try {
            SubmitClaimRespDto data = claimPublicService.submit(
                    token, fullName, phone, ageU20, idCardFile, receiptFile, parentFile
            );
            return new ResponseEntity<>(this.responseSuccess(data), HttpStatus.OK);
        } catch (CustomException e) {
            return new ResponseEntity<>(this.responseError(e.getCode(), e.getMessage(), e.getField()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            this.log.error("submit claim error", e);
            return new ResponseEntity<>(this.responseError(500, "Internal Server Error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
