package com.nestle.blend.api.controller;

import com.nestle.blend.api.dto.BaseResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

public class ABaseCtrl {

    protected HttpServletRequest req;
    protected HttpServletResponse res;

    public ABaseCtrl(HttpServletRequest req, HttpServletResponse res){
        this.req = req;
        this.res = res;
    }

    protected BaseResponseDto responseSuccess(Object obj){
        return new BaseResponseDto(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), obj);
    }
    protected BaseResponseDto responseError(int code, String message, Object obj){
        return new BaseResponseDto(code, message, obj);
    }
}
