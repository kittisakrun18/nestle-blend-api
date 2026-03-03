package com.nestle.blend.api.service;

import com.nestle.blend.api.dto.auth.AuthReqDto;
import com.nestle.blend.api.dto.auth.AuthRespDto;
import com.nestle.blend.api.dto.auth.ForgetPasswordReqDto;
import com.nestle.blend.api.entity.AdminRefreshTokenEntity;
import com.nestle.blend.api.entity.AdminUserEntity;
import com.nestle.blend.api.exception.CustomException;
import com.nestle.blend.api.repository.AdminRefreshTokenRepository;
import com.nestle.blend.api.repository.AdminUserRepository;
import com.nestle.blend.api.utils.HashUtil;
import com.nestle.blend.api.utils.StringUtils;
import com.nestle.blend.api.vo.MailRecipient;
import com.nestle.blend.api.vo.MailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuthService {

    @Value("${app.jwt.refresh-ttl-days}")
    private int refreshTtlDays;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminUserRepository adminUserRepository;
    @Autowired
    private AdminRefreshTokenRepository adminRefreshTokenRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired(required = false)
    private MailServices mailServices;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Admin login
     */
    @Transactional
    public AuthRespDto authen(AuthReqDto reqDto) throws Exception {
        if (reqDto == null || reqDto.getUsername() == null || reqDto.getPassword() == null) {
            throw new CustomException(400, "INVALID_REQUEST");
        }

        AdminUserEntity admin = adminUserRepository
                .findByUsernameIgnoreCase(reqDto.getUsername())
                .orElseThrow(() -> new CustomException(401, "user.notFound"));

        if (admin.getIsActive() != null && !admin.getIsActive()) {
            throw new CustomException(401, "user.inActive");
        }

        boolean passMatch = passwordEncoder.matches(reqDto.getPassword(), admin.getPasswordHash());
        if (!passMatch) {
            throw new CustomException(401, "user.inCorrect");
        }

        // update last login
        admin.setLastLoginAt(OffsetDateTime.now());
        adminUserRepository.save(admin);

        String access = jwtService.createAccessToken(admin.getId().toString(), admin.getUsername(), "ADMIN");
        String refreshRaw = newRefreshTokenString();

        saveRefreshToken(admin, refreshRaw, null, null);

        AuthRespDto result = new AuthRespDto();
        result.setCustomerId(admin.getId().toString());
        result.setUsername(admin.getUsername());
        result.setFullname(admin.getDisplayName() != null && !admin.getDisplayName().isBlank() ? admin.getDisplayName() : admin.getUsername());
        result.setAccessToken(access);
        result.setRefreshToken(refreshRaw);
        return result;
    }

    /**
     * Rotate refresh token and return new access + refresh
     */
    @Transactional
    public AuthRespDto refreshToken(String refreshTokenRaw) throws CustomException {
        if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) {
            throw new CustomException(401, "NO_REFRESH_TOKEN");
        }

        String tokenHash = HashUtil.sha256Hex(refreshTokenRaw);
        AdminRefreshTokenEntity tokenEntity = adminRefreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new CustomException(401, "INVALID_REFRESH_TOKEN"));

        // validate
        if (tokenEntity.getRevokedAt() != null) {
            throw new CustomException(401, "REFRESH_TOKEN_REVOKED");
        }
        if (tokenEntity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new CustomException(401, "REFRESH_TOKEN_EXPIRED");
        }

        AdminUserEntity admin = tokenEntity.getAdminUser();
        if (admin.getIsActive() != null && !admin.getIsActive()) {
            throw new CustomException(401, "user.inActive");
        }

        // rotate: revoke old and create new
        tokenEntity.setRevokedAt(OffsetDateTime.now());
        adminRefreshTokenRepository.save(tokenEntity);

        String newRefreshRaw = newRefreshTokenString();
        saveRefreshToken(admin, newRefreshRaw, tokenEntity.getCreatedIp(), tokenEntity.getUserAgent());

        String access = jwtService.createAccessToken(admin.getId().toString(), admin.getUsername(), "ADMIN");

        AuthRespDto result = new AuthRespDto();
        result.setCustomerId(admin.getId().toString());
        result.setUsername(admin.getUsername());
        result.setFullname(admin.getDisplayName() != null && !admin.getDisplayName().isBlank() ? admin.getDisplayName() : admin.getUsername());
        result.setAccessToken(access);
        result.setRefreshToken(newRefreshRaw);
        return result;
    }

    /**
     * Logout by revoking refresh token
     */
    @Transactional
    public void logout(String refreshTokenRaw) {
        if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) return;
        String tokenHash = HashUtil.sha256Hex(refreshTokenRaw);
        adminRefreshTokenRepository.findByTokenHash(tokenHash).ifPresent(t -> {
            if (t.getRevokedAt() == null) {
                t.setRevokedAt(OffsetDateTime.now());
                adminRefreshTokenRepository.save(t);
            }
        });
    }

    /**
     * Optional: reset password for admin by email.
     * If mail is not configured, it will still update password and return true.
     */
    @Transactional
    public boolean forgetPassword(ForgetPasswordReqDto reqDto) throws Exception {
        if (reqDto == null || reqDto.getEmail() == null || reqDto.getEmail().isBlank()) {
            throw new CustomException(400, "INVALID_REQUEST");
        }

        AdminUserEntity admin = adminUserRepository
                .findByEmailIgnoreCase(reqDto.getEmail())
                .orElseThrow(() -> new CustomException(401, "user.notFound"));

        if (admin.getIsActive() != null && !admin.getIsActive()) {
            throw new CustomException(401, "user.inActive");
        }

        String newPassword = StringUtils.randomString(10);
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        adminUserRepository.save(admin);

        // best-effort email
        if (mailServices != null && admin.getEmail() != null && !admin.getEmail().isBlank()) {
            MailVo mailVo = new MailVo();
            mailVo.setSubject("Reset Password");
            mailVo.setHtml(false);
            mailVo.setMessage("Your new password: " + newPassword);

            MailRecipient recipient = new MailRecipient();
            recipient.setRecipient(admin.getEmail());
            recipient.setRecipientName(admin.getDisplayName() != null ? admin.getDisplayName() : admin.getUsername());
            mailVo.setRecipients(List.of(recipient));

            mailServices.sendMail(mailVo);
        }

        return true;
    }

    // =========================
    // helpers
    // =========================

    private void saveRefreshToken(AdminUserEntity admin, String refreshTokenRaw, String ip, String userAgent) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime exp = now.plusDays(refreshTtlDays);

        AdminRefreshTokenEntity token = AdminRefreshTokenEntity.builder()
                .adminUser(admin)
                .tokenHash(HashUtil.sha256Hex(refreshTokenRaw))
                .issuedAt(now)
                .expiresAt(exp)
                .revokedAt(null)
                .createdIp(ip)
                .userAgent(userAgent)
                .build();

        adminRefreshTokenRepository.save(token);
    }

    private static String newRefreshTokenString() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        // URL-safe base64 without padding
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
