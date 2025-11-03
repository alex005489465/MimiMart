package com.mimimart.infrastructure.email.sender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * SMTP 郵件發送實作
 *
 * <p>使用 Spring Boot 的 JavaMailSender 透過 SMTP 協定發送郵件。
 * 適用於開發環境（Mailpit）或任何支援 SMTP 的郵件服務。
 *
 * @author MimiMart Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mimimart.email.provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${mimimart.email.from.address:${spring.mail.username}}")
    private String fromAddress;

    @Value("${mimimart.email.from.name:MimiMart}")
    private String fromName;

    @Override
    public void send(String to, String subject, String htmlContent) throws MessagingException {
        log.debug("使用 SMTP 發送郵件至: {}", to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            // 設定寄件者（支援顯示名稱）
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("SMTP 郵件發送成功: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("SMTP 郵件發送失敗: to={}, error={}", to, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("SMTP 郵件發送發生未預期錯誤: to={}", to, e);
            throw new MessagingException("SMTP 郵件發送失敗", e);
        }
    }
}
