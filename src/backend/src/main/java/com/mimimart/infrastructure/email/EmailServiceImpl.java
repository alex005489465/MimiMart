package com.mimimart.infrastructure.email;

import com.mimimart.application.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

/**
 * 郵件服務實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void sendWelcomeEmail(String email, String memberName) {
        try {
            Context context = new Context();
            context.setVariable("memberName", memberName);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("email/welcome", context);

            sendHtmlEmail(email, "歡迎加入 MimiMart！", htmlContent);
            log.info("歡迎郵件已發送至: {}", email);
        } catch (MessagingException e) {
            log.error("發送歡迎郵件失敗: {}", email, e);
            throw new RuntimeException("發送歡迎郵件失敗", e);
        }
    }

    @Override
    public void sendVerificationEmail(String email, String memberName, String verificationToken) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;

            Context context = new Context();
            context.setVariable("memberName", memberName);
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("token", verificationToken);

            String htmlContent = templateEngine.process("email/verification", context);

            sendHtmlEmail(email, "請驗證您的 Email", htmlContent);
            log.info("驗證郵件已發送至: {}", email);
        } catch (MessagingException e) {
            log.error("發送驗證郵件失敗: {}", email, e);
            throw new RuntimeException("發送驗證郵件失敗", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String memberName, String resetToken) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            Context context = new Context();
            context.setVariable("memberName", memberName);
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("token", resetToken);

            String htmlContent = templateEngine.process("email/password-reset", context);

            sendHtmlEmail(email, "重設您的密碼", htmlContent);
            log.info("密碼重設郵件已發送至: {}", email);
        } catch (MessagingException e) {
            log.error("發送密碼重設郵件失敗: {}", email, e);
            throw new RuntimeException("發送密碼重設郵件失敗", e);
        }
    }

    /**
     * 發送 HTML 格式郵件
     *
     * @param to 收件人
     * @param subject 主旨
     * @param htmlContent HTML 內容
     * @throws MessagingException 郵件發送例外
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
