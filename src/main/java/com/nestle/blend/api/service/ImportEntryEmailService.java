package com.nestle.blend.api.service;

import com.nestle.blend.api.constant.EmailStatus;
import com.nestle.blend.api.entity.ImportEntryEntity;
import com.nestle.blend.api.repository.ImportEntryRepository;
import com.nestle.blend.api.utils.DateUtils;
import com.nestle.blend.api.utils.HashUtil;
import com.nestle.blend.api.utils.MessageUtils;
import com.nestle.blend.api.utils.StringUtils;
import com.nestle.blend.api.vo.MailRecipient;
import com.nestle.blend.api.vo.MailVo;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ImportEntryEmailService {

    private final ImportEntryRepository importEntryRepository;
    private final MailServices mailServices;

    @Value("${nestle-front.base-url:http://localhost:3000}")
    private String claimFrontendBaseUrl;

    @Value("${app.claim.link.ttl-hours:168}")
    private int linkTtlHours;

    @Value("${app.email.template.claim:claim-link.html.ftl}")
    private String claimLinkHtmlTemplate;

    @Autowired
    private MessageUtils messageUtils;

    public ImportEntryEmailService(
            ImportEntryRepository importEntryRepository,
            MailServices mailServices
    ) {
        this.importEntryRepository = importEntryRepository;
        this.mailServices = mailServices;
    }

    /**
     * ส่งอีเมลหลัง import (ใช้ rawToken ที่เพิ่งสร้าง)
     * - ป้องกันซ้ำ: ถ้า status = SENT จะไม่ส่งซ้ำ
     * - บันทึก status SENT/FAILED + emailSentAt/emailError
     */
    @Transactional
    public void sendAfterImport(UUID importEntryId) {
        ImportEntryEntity entry = importEntryRepository.findByIdForUpdate(importEntryId)
                .orElseThrow(() -> new IllegalArgumentException("importEntry not found: " + importEntryId));

        if (EmailStatus.SENT.equalsIgnoreCase(entry.getEmailStatus())) {
            return;
        }

        try {
            Map<String, Object> params = this.mappingParams(entry);

            String html = mailServices.getEmailContent(claimLinkHtmlTemplate, params);

            // ส่งเป็น HTML เป็นหลัก
            MailVo mail = new MailVo(true,
                    List.of(new MailRecipient(entry.getFullName(), entry.getEmail())),
                    null,
                    (String) params.get("subject"),
                    html
            );

            mailServices.sendMail(mail);

            entry.setEmailStatus(EmailStatus.SENT);
            entry.setEmailError(null);
            entry.setEmailSentAt(LocalDateTime.now());
            importEntryRepository.save(entry);
        } catch (MessagingException | IOException | TemplateException e) {
            e.printStackTrace();
            entry.setEmailStatus(EmailStatus.FAILED);
            entry.setEmailError(safeMsg(e));
            importEntryRepository.save(entry);
        }
    }

    /**
     * ตรวจเช็คซ้ำ (re-check) เพื่อส่งอีเมลให้รายการที่ยัง PENDING/FAILED
     * แนวคิดกันซ้ำ:
     * - ดึงเฉพาะ PENDING/FAILED
     * - ล็อคแถว (PESSIMISTIC_WRITE) ตอนส่ง
     * - ถ้าเคย SENT แล้วจะข้าม
     * <p>
     * กรณี link หมดอายุ: จะออก token ใหม่, อัปเดต tokenHash/issuedAt/expiresAt แล้วค่อยส่ง
     */
    @Transactional
    public int resendPendingOrFailed(int limit) {
        List<String> statuses = Arrays.asList(EmailStatus.PENDING, EmailStatus.FAILED);

        Page<ImportEntryEntity> page = importEntryRepository.findByEmailStatusInAndUsedAtIsNull(statuses, PageRequest.of(0, Math.max(1, limit)));
        int sent = 0;

        for (ImportEntryEntity e : page.getContent()) {
            // lock row each time
            ImportEntryEntity entry = importEntryRepository.findByIdForUpdate(e.getId()).orElse(null);
            if (entry == null) continue;

            if (EmailStatus.SENT.equalsIgnoreCase(entry.getEmailStatus())) continue; // already sent by other thread

            // generate fresh token each resend (so we don't need to store rawToken)
            String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();
            String tokenHash = HashUtil.sha256Hex(rawToken);

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Bangkok"));
            entry.setTokenHash(tokenHash);
            entry.setIssuedAt(now);
            entry.setExpiresAt(now.plusHours(linkTtlHours));
            entry.setEmailStatus(EmailStatus.PENDING);
            entry.setEmailError(null);
            importEntryRepository.save(entry);

            // send mail (reuse same method but avoid recursion)
            try {
                Map<String, Object> params = this.mappingParams(entry);

                String html = mailServices.getEmailContent(claimLinkHtmlTemplate, params);

                MailVo mail = new MailVo(true,
                        List.of(new MailRecipient(entry.getFullName(), entry.getEmail())),
                        null,
                        (String) params.get("subject"),
                        html
                );
                mailServices.sendMail(mail);

                entry.setEmailStatus(EmailStatus.SENT);
                entry.setEmailError(null);
                entry.setEmailSentAt(LocalDateTime.now());
                importEntryRepository.save(entry);

                sent++;
            } catch (Exception ex) {
                entry.setEmailStatus(EmailStatus.FAILED);
                entry.setEmailError(safeMsg(ex));
                importEntryRepository.save(entry);
            }
        }

        return sent;
    }

    private Map<String, Object> mappingParams(ImportEntryEntity entry) throws IOException {
        String link = buildClaimLink(entry.getTokenHash());

        ClassPathResource resource = new ClassPathResource("templates/logo.png");
        byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String base64Logo = Base64.getEncoder().encodeToString(bytes);

        Map<String, Object> params = new HashMap<>();
        String key = "email.subject";
        String claimLinkSubject = this.messageUtils.getMessage(key);
        claimLinkSubject = StringUtils.checkNotEmpty(claimLinkSubject) ? claimLinkSubject : "Nescafe Blend & Brew: Make This Moment MORE";

        params.put("subject", claimLinkSubject);
        params.put("winnerName", entry.getFullName());
        params.put("seatNo", entry.getSeqNo());
        params.put("zone", entry.getZone());
        params.put("purchaseChannel", entry.getCategory().getName());
        params.put("receiptInfo", entry.getReward());
        params.put("purchaseDate", DateUtils.localeDateToThaiStr(entry.getPurchasedAt()));
        params.put("deadlineDate", DateUtils.localeDateTimeToThaiStr(entry.getExpiresAt()));
        params.put("deadlineTime", DateUtils.localeDateTimeToTimeStr(entry.getExpiresAt()));
        params.put("claimLink", link);
        params.put("logo", base64Logo);

        return params;
    }

    private String buildClaimLink(String rawToken) {
        // main entry ของ frontend: /?token=...
        String base = claimFrontendBaseUrl;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "?token=" + rawToken;
    }

    private String safeMsg(Exception e) {
        String m = e.getMessage();
        if (m == null) return e.getClass().getSimpleName();
        return m.length() > 1000 ? m.substring(0, 1000) : m;
    }
}
