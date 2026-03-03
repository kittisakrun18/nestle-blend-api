package com.nestle.blend.api.exception;

import com.nestle.blend.api.dto.exception.FieldErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

public class CustomBindingException extends BindException {

    private int code;
    private String message = "";
    private List<FieldErrorDto> errors;

    public CustomBindingException(BindingResult bindingResult) {
        super(bindingResult);

        this.code = HttpStatus.BAD_REQUEST.value();
        this.message = HttpStatus.BAD_GATEWAY.getReasonPhrase();
        List<FieldError> errors = bindingResult.getFieldErrors();
        if (errors != null && errors.size() > 0) {
            this.errors = new ArrayList<>();
            FieldErrorDto errorDto = null;
            for (FieldError e : errors) {
                errorDto = new FieldErrorDto(e.getField(), e.getField() + " " + e.getDefaultMessage());
                this.errors.add(errorDto);
            }
        }
    }

    public String getMessage() {
        return this.message;
    }

    public int getCode() {
        return this.code;
    }

    public List<FieldErrorDto> getErrors() {
        return this.errors;
    }
}
