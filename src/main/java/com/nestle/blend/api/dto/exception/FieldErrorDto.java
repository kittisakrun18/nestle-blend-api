package com.nestle.blend.api.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorDto implements Serializable {

    private String field;
    private String message;

}
