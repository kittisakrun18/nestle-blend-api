package com.nestle.blend.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRespDto implements Serializable {

    private String customerId;
    private String username;
    private String fullname;
    private String accessToken;
    @JsonIgnore
    private String refreshToken;

}
