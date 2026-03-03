package com.nestle.blend.api.controller;

import com.nestle.blend.api.dto.BaseResponseDto;
import com.nestle.blend.api.dto.CommonValueLabelDto;
import com.nestle.blend.api.service.MasterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "${app.api.path.prefix}/master")
public class MasterCtrl extends ABaseCtrl {

    @Autowired
    private MasterService masterService;

    public MasterCtrl(HttpServletRequest req, HttpServletResponse res) {
        super(req, res);
    }

    @GetMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<List<CommonValueLabelDto>>> getCategories() {
        List<CommonValueLabelDto> results = null;
        try {
            results = this.masterService.getCategories();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(super.responseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }

        return ResponseEntity.ok(super.responseSuccess(results));
    }
}
