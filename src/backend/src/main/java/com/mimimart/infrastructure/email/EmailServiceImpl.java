package com.mimimart.infrastructure.email;

import com.mimimart.application.service.EmailQuotaService;
import com.mimimart.application.service.EmailRateLimitService;
import com.mimimart.application.service.EmailSendLogService;
import com.mimimart.application.service.EmailService;
import com.mimimart.infrastructure.email.sender.EmailSender;
import com.mimimart.shared.valueobject.EmailType;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * 郵件服務實作
 *
 * <p>採用策略模式委派給 {@link EmailSender} 處理實際的郵件發送，
 * 支援多種發送方式（SMTP、AWS SES 等）的動態切換。
 *
 * @author MimiMart Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;
    private final EmailQuotaService emailQuotaService;
    private final EmailRateLimitService emailRateLimitService;
    private final EmailSendLogService emailSendLogService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void sendWelcomeEmail(String email, String memberName) {
        String subject = "歡迎加入 MimiMart！";

        // 檢查月度配額
        emailQuotaService.checkAndIncrementQuota();

        try {
            Context context = new Context();
            context.setVariable("memberName", memberName);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("email/welcome", context);

            sendHtmlEmail(email, subject, htmlContent);

            // 記錄發送成功
            emailSendLogService.logEmailSent(null, email, EmailType.WELCOME, subject);
            log.info("歡迎郵件已發送至: {}", email);
        } catch (MessagingException e) {
            // 記錄發送失敗
            emailSendLogService.logEmailFailed(null, email, EmailType.WELCOME, subject, e.getMessage());
            log.error("發送歡迎郵件失敗: {}", email, e);
            throw new RuntimeException("發送歡迎郵件失敗", e);
        }
    }

    @Override
    public void sendVerificationEmail(String email, String memberName, String verificationToken) {
        String subject = "請驗證您的 Email";

        // 檢查月度配額
        emailQuotaService.checkAndIncrementQuota();

        // 註：會員頻率限制在 AuthService 層檢查，因為這裡沒有 memberId

        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;

            Context context = new Context();
            context.setVariable("memberName", memberName);
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("token", verificationToken);

            String htmlContent = templateEngine.process("email/verification", context);

            sendHtmlEmail(email, subject, htmlContent);

            // 記錄發送成功
            emailSendLogService.logEmailSent(null, email, EmailType.VERIFICATION, subject);
            log.info("驗證郵件已發送至: {}", email);
        } catch (MessagingException e) {
            // 記錄發送失敗
            emailSendLogService.logEmailFailed(null, email, EmailType.VERIFICATION, subject, e.getMessage());
            log.error("發送驗證郵件失敗: {}", email, e);
            throw new RuntimeException("發送驗證郵件失敗", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String memberName, String resetToken) {
        String subject = "重設您的密碼";

        // 檢查月度配額
        emailQuotaService.checkAndIncrementQuota();

        // 註：會員頻率限制在 AuthService 層檢查，因為這裡沒有 memberId

        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            Context context = new Context();
            context.setVariable("memberName", memberName);
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("token", resetToken);

            String htmlContent = templateEngine.process("email/password-reset", context);

            sendHtmlEmail(email, subject, htmlContent);

            // 記錄發送成功
            emailSendLogService.logEmailSent(null, email, EmailType.PASSWORD_RESET, subject);
            log.info("密碼重設郵件已發送至: {}", email);
        } catch (MessagingException e) {
            // 記錄發送失敗
            emailSendLogService.logEmailFailed(null, email, EmailType.PASSWORD_RESET, subject, e.getMessage());
            log.error("發送密碼重設郵件失敗: {}", email, e);
            throw new RuntimeException("發送密碼重設郵件失敗", e);
        }
    }

    /**
     * 發送 HTML 格式郵件（委派給策略實作）
     *
     * @param to 收件人
     * @param subject 主旨
     * @param htmlContent HTML 內容
     * @throws MessagingException 郵件發送例外
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        emailSender.send(to, subject, htmlContent);
    }
}
