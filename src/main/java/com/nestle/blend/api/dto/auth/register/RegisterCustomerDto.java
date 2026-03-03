package com.nestle.blend.api.dto.auth.register;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCustomerDto implements Serializable {

    private String legalName;
    private String legalNo;
    private String legalAddress;
    private String fullname;
    @NotNull
    @NotEmpty
    private String cardId;
    private int age;
    private String nationality;
    private String address;
    private String officeAddress;
    private String phoneNo;
    @NotNull
    @NotEmpty
    private String email;
}
