package com.nestle.blend.api.service;

import com.nestle.blend.api.dto.claim.SubmitClaimRespDto;
import com.nestle.blend.api.dto.claim.ValidateTokenRespDto;
import com.nestle.blend.api.entity.ClaimSubmissionEntity;
import com.nestle.blend.api.entity.ImportEntryEntity;
import com.nestle.blend.api.exception.CustomException;
import com.nestle.blend.api.repository.ClaimSubmissionRepository;
import com.nestle.blend.api.repository.ImportEntryRepository;
import com.nestle.blend.api.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClaimPublicService {

    private final ImportEntryRepository importEntryRepository;
    private final ClaimSubmissionRepository claimSubmissionRepository;

    @Value("${app.upload.path}")
    private String uploadPath;

    @Autowired
    private MessageUtils messageUtils;

    public ClaimPublicService(ImportEntryRepository importEntryRepository,
                              ClaimSubmissionRepository claimSubmissionRepository) {
        this.importEntryRepository = importEntryRepository;
        this.claimSubmissionRepository = claimSubmissionRepository;
    }

    public ValidateTokenRespDto validateToken(String token) throws CustomException {
        ImportEntryEntity entry = validateTokenAndGetEntry(token);

        return new ValidateTokenRespDto(
                entry.getId(),
                entry.getEmail(),
                entry.getCategory() != null ? entry.getCategory().getName() : null,
                entry.getSeqNo(),
                entry.getExpiresAt()
        );
    }

    @Transactional
    public SubmitClaimRespDto submit(String token,
                                     String fullName,
                                     String phone,
                                     boolean ageU20,
                                     MultipartFile idCardFile,
                                     MultipartFile receiptFile,
                                     MultipartFile parentFile) throws Exception {
        ImportEntryEntity entry = validateTokenAndGetEntry(token);

        // Validation fields
        if (fullName == null || fullName.trim().isEmpty()) {
            String key = "claim.required.fullName";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "fullName");
        }
        if (phone == null || phone.trim().isEmpty()) {
            String key = "claim.required.phone";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "phone");
        }
        String phoneDigits = phone.replaceAll("\\D", "");
        if (!phoneDigits.matches("^\\d{8,15}$")) {
            String key = "claim.format.phone";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "phone");
        }

        if (idCardFile == null || idCardFile.isEmpty()) {
            String key = "claim.required.idCardFile";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "idCardFile");
        }
        if (receiptFile == null || receiptFile.isEmpty()) {
            String key = "claim.required.receiptFile";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "receiptFile");
        }
        if (ageU20 && (parentFile == null || parentFile.isEmpty())) {
            String key = "claim.required.parentIdCardFile";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "parentFile");
        }

        // save files under upload root (relative keys) -> ResourceCtrl can download via base64(fileKey)
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(root);

        Path claimDir = root.resolve("claims").resolve(entry.getId().toString());
        Files.createDirectories(claimDir);

        String idCardKey = storeTo(claimDir, idCardFile, "idcard");
        String receiptKey = storeTo(claimDir, receiptFile, "receipt");
        String parentPath = null;
        if (ageU20) {
            parentPath = storeTo(claimDir, parentFile, "parent");
        }

        // prevent duplicate submit (extra guard)
        Optional<ClaimSubmissionEntity> existed = claimSubmissionRepository.findByImportEntry_Id(entry.getId());
        if (existed.isPresent()) {
            String key = "claim.token.sent";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "token");
        }

        LocalDateTime now = LocalDateTime.now();

        String ageU20Str = ageU20 ? "Y" : "N";

        ClaimSubmissionEntity entity = ClaimSubmissionEntity.builder()
                .importEntry(entry)
                .fullName(fullName.trim())
                .phone(phoneDigits)
                .idCardFilePath(idCardKey)
                .ageU20(ageU20Str)
                .receiptFilePath(receiptKey)
                .parentFilePath(parentPath)
                .submittedAt(now)
                .build();

        ClaimSubmissionEntity saved = claimSubmissionRepository.save(entity);

        // mark token as used
        entry.setUsedAt(now);
        importEntryRepository.save(entry);

        return new SubmitClaimRespDto(
                saved.getId()
        );
    }

    private ImportEntryEntity validateTokenAndGetEntry(String token) throws CustomException {
        if (token == null || token.trim().isEmpty()) {
            String key = "claim.token.notFound";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "token");
        }

        String unauthorizedKey = "claim.token.unauthorized";
        String tokenHash = token.trim();
        ImportEntryEntity entry = importEntryRepository.findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new CustomException(this.messageUtils.getCode(unauthorizedKey), this.messageUtils.getMessage(unauthorizedKey), "token")
                );

        LocalDateTime now = LocalDateTime.now();
        if (entry.getExpiresAt() != null && entry.getExpiresAt().isBefore(now)) {
            String key = "claim.token.expired";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "token");
        }
        if (entry.getUsedAt() != null) {
            String key = "claim.token.used";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "token");
        }
        if (claimSubmissionRepository.findByImportEntry_Id(entry.getId()).isPresent()) {
            String key = "claim.token.sent";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "token");
        }
        return entry;
    }

    private String storeTo(Path claimDir, MultipartFile file, String prefix) throws Exception {
        String original = (file.getOriginalFilename() == null) ? "file" : file.getOriginalFilename();
        String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = prefix + "-" + UUID.randomUUID() + "-" + safe;

        Path fullPath = claimDir.resolve(filename).normalize();
        // ensure inside claimDir
        if (!fullPath.startsWith(claimDir)) {
            String key = "claim.filename.inCorrect";
            throw new CustomException(this.messageUtils.getCode(key), this.messageUtils.getMessage(key), "file");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, fullPath);
        }

        // relative key from upload root: claims/{entryId}/{filename}
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path relative = root.relativize(fullPath);
        return relative.toString().replace("\\", "/");
    }
}
