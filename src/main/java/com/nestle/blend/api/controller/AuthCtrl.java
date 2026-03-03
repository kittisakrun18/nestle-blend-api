package com.nestle.blend.api.controller;

import com.nestle.blend.api.dto.BaseResponseDto;
import com.nestle.blend.api.dto.auth.AuthReqDto;
import com.nestle.blend.api.dto.auth.AuthRespDto;
import com.nestle.blend.api.dto.auth.ForgetPasswordReqDto;
import com.nestle.blend.api.exception.CustomBindingException;
import com.nestle.blend.api.exception.CustomException;
import com.nestle.blend.api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping(value = "${app.api.path.prefix}/auth")
public class AuthCtrl extends ABaseCtrl {
    private Logger log = LogManager.getLogger(this.getClass());

    @Value("${app.http.secure}")
    private boolean appHttpSecure;
    @Value("${app.http.secure.sameSite}")
    private String appHttpSecureSameSite;
    @Value("${app.jwt.refresh-ttl-days}")
    private int appRefreshDay;

    @Autowired
    private AuthService authService;

    public AuthCtrl(HttpServletRequest req, HttpServletResponse res) {
        super(req, res);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<AuthRespDto>> create(@Valid @RequestBody AuthReqDto dto, BindingResult bindingResult) {
        AuthRespDto result = null;
        try {
            this.log.info("Request Body : {}", dto);
            if (bindingResult.hasErrors()) {
                throw new CustomBindingException(bindingResult);
            }
            result = this.authService.authen(dto);
            this.log.info("Result : {}", result);

            // refresh token -> HttpOnly cookie (web)
            ResponseCookie cookie = ResponseCookie.from("api_refresh_token", result.getRefreshToken())
                    .httpOnly(true)
                    .secure(this.appHttpSecure) // production: true (https)
                    .sameSite(this.appHttpSecureSameSite)
                    .path("/api") // refresh/logout ใช้ cookie นี้
                    .maxAge(Duration.ofDays(this.appRefreshDay))
                    .build();

            return ResponseEntity.ok()
                    .header("Set-Cookie", cookie.toString())
                    .body(super.responseSuccess(result));
        } catch (CustomBindingException e) {
            e.printStackTrace();
            this.log.error(e);
            return new ResponseEntity<>(super.responseError(e.getCode(), e.getMessage(), e.getErrors()), HttpStatus.BAD_REQUEST);
        } catch (CustomException e) {
            e.printStackTrace();
            this.log.error(e);
            return new ResponseEntity<>(super.responseError(e.getCode(), e.getMessage(), null), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            e.printStackTrace();
            this.log.error(e);
            return ResponseEntity.internalServerError().body(super.responseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }
    }

    @PutMapping(value = "/forget-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponseDto<Boolean>> forgetPassword(@Valid @RequestBody ForgetPasswordReqDto dto, BindingResult bindingResult) {
        boolean result = false;
        try {
            this.log.info("Request Body : {}", dto);
            if (bindingResult.hasErrors()) {
                throw new CustomBindingException(bindingResult);
            }
            result = this.authService.forgetPassword(dto);
            this.log.info("Result : {}", result);
        } catch (CustomBindingException e) {
            e.printStackTrace();
            this.log.error(e);
            return new ResponseEntity<>(super.responseError(e.getCode(), e.getMessage(), e.getErrors()), HttpStatus.BAD_REQUEST);
        } catch (CustomException e) {
            e.printStackTrace();
            this.log.error(e);
            return new ResponseEntity<>(super.responseError(e.getCode(), e.getMessage(), null), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            e.printStackTrace();
            this.log.error(e);
            return ResponseEntity.internalServerError().body(super.responseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }

        return ResponseEntity.ok(super.responseSuccess(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "api_refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "NO_REFRESH_COOKIE"));
        }

        AuthRespDto tokens = null;
        try {
            tokens = authService.refreshToken(refreshToken);
        } catch (CustomException e) {
            return ResponseEntity.status(401).body(Map.of("error", "NO_REFRESH_COOKIE"));
        }

        ResponseCookie cookie = ResponseCookie.from("api_refresh_token", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(this.appHttpSecure)
                .sameSite(this.appHttpSecureSameSite)
                .path("/api")
                .maxAge(Duration.ofDays(this.appRefreshDay))
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(Map.of("accessToken", tokens.getAccessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "api_refresh_token", required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        // clear cookie
        ResponseCookie clear = ResponseCookie.from("api_refresh_token", "")
                .httpOnly(true)
                .secure(this.appHttpSecure) // production: true
                .sameSite(this.appHttpSecureSameSite)
                .path("/api")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", clear.toString())
                .body(Map.of("ok", true));
    }
}
