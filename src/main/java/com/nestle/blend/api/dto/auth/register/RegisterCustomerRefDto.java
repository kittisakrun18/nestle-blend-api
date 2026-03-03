package com.nestle.blend.api.dto.auth.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCustomerRefDto implements Serializable {

    private String fullname;
    private String cardId;
    private String nationality;
    private String address;
    private String phoneNo;
    private String email;

}
