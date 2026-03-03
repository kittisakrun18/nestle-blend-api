package com.nestle.blend.api.dto.auth;

import com.nestle.blend.api.dto.auth.register.RegisterCustomerDto;
import com.nestle.blend.api.dto.auth.register.RegisterCustomerRefDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReqDto implements Serializable {

    @Valid
    private RegisterCustomerDto customer;
    private RegisterCustomerRefDto ref1;
}
