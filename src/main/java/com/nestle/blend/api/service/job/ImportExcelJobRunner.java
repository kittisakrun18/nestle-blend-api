package com.nestle.blend.api.service.job;

import com.nestle.blend.api.constant.EmailStatus;
import com.nestle.blend.api.dto.admin.ImportJobStatusRespDto;
import com.nestle.blend.api.entity.AdminUserEntity;
import com.nestle.blend.api.entity.CategoryEntity;
import com.nestle.blend.api.entity.ImportEntryEntity;
import com.nestle.blend.api.entity.ImportEntryFailEntity;
import com.nestle.blend.api.repository.AdminUserRepository;
import com.nestle.blend.api.repository.CategoryRepository;
import com.nestle.blend.api.repository.ImportEntryFailRepository;
import com.nestle.blend.api.repository.ImportEntryRepository;
import com.nestle.blend.api.service.ImportEntryEmailService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ImportExcelJobRunner {

    private final CategoryRepository categoryRepository;
    private final ImportEntryRepository importEntryRepository;
    private final ImportEntryFailRepository importEntryFailRepository;
    private final AdminUserRepository adminUserRepository;

    private final ImportEntryEmailService importEntryEmailService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public ImportExcelJobRunner(CategoryRepository categoryRepository,
                               ImportEntryRepository importEntryRepository,
                               ImportEntryFailRepository importEntryFailRepository,
                               AdminUserRepository adminUserRepository,
                               ImportEntryEmailService importEntryEmailService) {
        this.categoryRepository = categoryRepository;
        this.importEntryRepository = importEntryRepository;
        this.importEntryFailRepository = importEntryFailRepository;
        this.adminUserRepository = adminUserRepository;
        this.importEntryEmailService = importEntryEmailService;
    }

    @Async("importExecutor")
    public void runJobAsync(String jobId, Path filePath, UUID adminUserId, Map<String, ImportJobStatusRespDto> jobs) {
        ImportJobStatusRespDto st = jobs.get(jobId);
        if (st == null) return;

        try {
            st.setStatus("RUNNING");

            AdminUserEntity admin = null;
            if (adminUserId != null) {
                admin = adminUserRepository.findById(adminUserId).orElse(null);
            }

            int totalSheets = 0;
            int totalRows = 0;
            int inserted = 0;
            int skipped = 0;
            int failedRows = 0;

            final ZoneId zoneId = ZoneId.of("Asia/Bangkok");
            final LocalDate insertDt = LocalDate.now(zoneId);

            try (InputStream is = Files.newInputStream(filePath);
                 Workbook wb = new XSSFWorkbook(is)) {

                DataFormatter formatter = new DataFormatter();

                totalSheets = wb.getNumberOfSheets();
                st.setTotalSheets(totalSheets);

                for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                    Sheet sheet = wb.getSheetAt(s);
                    if (sheet == null) continue;

                    // row1 (index 0) = category name
                    Row r0 = sheet.getRow(0);
                    String categoryName = getCellString(r0, 0, formatter);
                    if (categoryName == null || categoryName.isBlank()) {
                        continue;
                    }

                    final String catName = categoryName.trim();

                    CategoryEntity category = categoryRepository.findByNameIgnoreCase(catName)
                            .orElseGet(() -> categoryRepository.save(
                                    CategoryEntity.builder().name(catName).build()
                            ));

                    // row2 (index 1) = header -> skip
                    int lastRow = sheet.getLastRowNum();
                    for (int i = 3; i <= lastRow; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        // columns: seq_no, full_name, email, zone, reward, purchased_at
                        String seqNoRaw = getCellString(row, 0, formatter);
                        String fullName = getCellString(row, 1, formatter);
                        String email = getCellString(row, 2, formatter);
                        String zone = getCellString(row, 3, formatter);
                        String reward = getCellString(row, 4, formatter);
                        LocalDate purchasedAt = getCellDate(row, 5, formatter);

                        // Skip completely empty rows (all fields empty)
                        boolean allEmpty = isBlank(seqNoRaw)
                                && isBlank(fullName)
                                && isBlank(email)
                                && isBlank(zone)
                                && isBlank(reward)
                                && purchasedAt == null;
                        if (allEmpty) {
                            continue;
                        }

						// Validate required fields
						final String sheetName = sheet.getSheetName();
						final int excelRowNo = i + 1;
						final String purchasedAtRaw = getCellString(row, 5, formatter);
						List<String> rowErrors = new ArrayList<>();

						if (isBlank(seqNoRaw)) {
							rowErrors.add("seqNo is required");
						}

						if (isBlank(fullName)) {
							rowErrors.add("fullName is required");
						}

						if (isBlank(email)) {
							rowErrors.add("email is required");
						} else {
							String e = email.trim();
							if (e.length() > 254 || !EMAIL_PATTERN.matcher(e).matches()) {
								rowErrors.add("email format is invalid (value='" + email + "')");
							}
						}

						if (isBlank(zone)) {
							rowErrors.add("zone is required");
						}

						if (isBlank(reward)) {
							rowErrors.add("reward is required");
						}

						if (purchasedAt == null) {
							rowErrors.add("purchasedAt is required and must be a valid date (value='" + (purchasedAtRaw == null ? "" : purchasedAtRaw) + "')");
						}

						totalRows++;

						// If validation failed -> save fail record and continue (do not stop the whole job)
						if (!rowErrors.isEmpty()) {
							importEntryFailRepository.save(ImportEntryFailEntity.builder()
									.jobId(jobId)
									.insertDate(insertDt)
									.categoryName(catName)
									.sheetName(sheetName)
									.excelRowNo(excelRowNo)
									.seqNoRaw(seqNoRaw)
									.fullName(fullName)
									.email(email)
									.zone(zone)
									.reward(reward)
									.purchasedAtRaw(purchasedAtRaw)
									.reason(String.join(" | ", rowErrors))
                                    .createdAt(LocalDateTime.now())
									.build());
							failedRows++;
							skipped++;
							st.setTotalRows(totalRows);
							st.setInsertedRows(inserted);
							st.setSkippedRows(skipped);
							st.setFailedRows(failedRows);
							continue;
						}

						// prevent duplicates by email (unique at DB)
						if (email != null && importEntryRepository.findByEmailIgnoreCase(email.trim()).isPresent()) {
							importEntryFailRepository.save(ImportEntryFailEntity.builder()
									.jobId(jobId)
									.insertDate(insertDt)
									.categoryName(catName)
									.sheetName(sheetName)
									.excelRowNo(excelRowNo)
									.seqNoRaw(seqNoRaw)
									.fullName(fullName)
									.email(email)
									.zone(zone)
									.reward(reward)
									.purchasedAtRaw(purchasedAtRaw)
									.reason("duplicate email")
                                    .createdAt(LocalDateTime.now())
									.build());
							failedRows++;
							skipped++;
							st.setFailedRows(failedRows);
							continue;
						}

                        // generate link token (raw not stored)
                        String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();
                        String tokenHash = sha256Hex(rawToken);

                        LocalDateTime expires = this.mappingExpireDt();
                        LocalDateTime now = LocalDateTime.now(zoneId);

                        ImportEntryEntity entity = ImportEntryEntity.builder()
                                .category(category)
                                .seqNo(seqNoRaw)
                                .fullName(fullName.trim())
                                .email(email.trim())
                                .zone(zone.trim())
                                .reward(reward.trim())
                                .purchasedAt(purchasedAt)
                                .emailStatus(EmailStatus.PENDING)
                                .tokenHash(tokenHash)
                                .issuedAt(now)
                                .expiresAt(expires)
                                .createdBy(admin)
                                .build();

                        try {
                            ImportEntryEntity saved = importEntryRepository.save(entity);
                            inserted++;

                            try {
                                // send mail after import (prevent duplicate via emailStatus)
                                importEntryEmailService.sendAfterImport(saved.getId());
                            } catch (Exception ex){
                                ex.printStackTrace();
                                entity.setEmailStatus(EmailStatus.FAILED);
                                entity.setEmailError(ex.getMessage());
                                importEntryRepository.save(entity);
                            }
                        } catch (Exception ex) {
							// e.g. unique violation seq_no/email/token
							importEntryFailRepository.save(ImportEntryFailEntity.builder()
									.jobId(jobId)
									.insertDate(insertDt)
									.categoryName(catName)
									.sheetName(sheetName)
									.excelRowNo(excelRowNo)
									.seqNoRaw(seqNoRaw)
									.fullName(fullName)
									.email(email)
									.zone(zone)
									.reward(reward)
									.purchasedAtRaw(purchasedAtRaw)
									.reason("db insert failed: " + safeMsg(ex))
                                    .createdAt(LocalDateTime.now())
									.build());
							failedRows++;
							skipped++;
                        }

                        // update progress
                        st.setTotalRows(totalRows);
                        st.setInsertedRows(inserted);
                        st.setSkippedRows(skipped);
						st.setFailedRows(failedRows);
                    }
                }
            }

            st.setTotalRows(totalRows);
            st.setInsertedRows(inserted);
            st.setSkippedRows(skipped);
			st.setFailedRows(failedRows);
			st.setStatus(failedRows > 0 ? "SUCCESS_WITH_ERRORS" : "SUCCESS");

        } catch (Exception e) {
            st.setStatus("FAILED");
            st.setErrorMessage(e.getMessage());
            // Ensure DB changes rollback when validation/import fails
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e);
        } finally {
            // cleanup uploaded temp file
            try { Files.deleteIfExists(filePath); } catch (Exception ignored) {}
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

	private static String safeMsg(Exception ex) {
		String m = ex.getMessage();
		if (m == null) return ex.getClass().getSimpleName();
		m = m.replace("\n", " ").replace("\r", " ").trim();
		return m.length() > 900 ? m.substring(0, 900) : m;
	}

    private static String getCellString(Row row, int col, DataFormatter formatter) {
        if (row == null) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String v = formatter.formatCellValue(cell);
        return v != null ? v.trim() : null;
    }

    private static Integer getCellInteger(Row row, int col, DataFormatter formatter) {
        String s = getCellString(row, col, formatter);
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.replaceAll(",", "")); } catch (Exception e) { return null; }
    }

    private static LocalDate getCellDate(Row row, int col, DataFormatter formatter) {
        if (row == null) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (Exception ignored) {}

        String s = formatter.formatCellValue(cell);
        if (s == null || s.isBlank()) return null;

        // try common formats
        String[] patterns = new String[]{"d/M/yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "d-M-yyyy", "dd-MM-yyyy"};
        for (String p : patterns) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(p);
                return LocalDate.parse(s.trim(), fmt);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDateTime mappingExpireDt() {
        LocalDateTime result = LocalDateTime.of(2026, 3, 14, 13, 0);
        LocalDateTime currentDt = LocalDateTime.now();
        LocalDateTime round2Dt = LocalDateTime.of(2026, 3, 13, 17, 0);
        if (currentDt.isBefore(round2Dt)) {
            result = LocalDateTime.of(2026, 3, 13, 17, 0);
        }

        return result;
    }
}
