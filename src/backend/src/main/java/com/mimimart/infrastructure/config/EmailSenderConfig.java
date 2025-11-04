package com.mimimart.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 郵件發送策略配置
 *
 * <p>根據 {@code mimimart.email.provider} 配置項自動選擇郵件發送策略：
 * <ul>
 *   <li><b>smtp</b> (預設): 使用 SMTP 協定發送 (開發環境/Mailpit)</li>
 *   <li><b>resend</b>: 使用 Resend API 發送 (生產環境)</li>
 *   <li><b>aws-ses</b>: 使用 AWS SES SDK 發送 (已廢棄)</li>
 * </ul>
 *
 * <p>策略選擇透過 Spring Boot 的 {@code @ConditionalOnProperty} 機制實現，
 * 確保同一時間只會載入一個 {@link com.mimimart.infrastructure.email.sender.EmailSender} 實作。
 *
 * <h3>配置範例</h3>
 * <pre>
 * # 開發環境 - 使用 SMTP
 * mimimart.email.provider=smtp
 * spring.mail.host=mailpit
 * spring.mail.port=1025
 *
 * # 生產環境 - 使用 Resend
 * mimimart.email.provider=resend
 * mimimart.email.resend.api-key=re_xxxxx
 * mimimart.email.from.address=noreply@yourdomain.com
 *
 * # 生產環境 - 使用 AWS SES (已廢棄)
 * mimimart.email.provider=aws-ses
 * mimimart.email.aws.ses.region=ap-south-1
 * AWS_ACCESS_KEY_ID=AKIA...
 * AWS_SECRET_ACCESS_KEY=xxx...
 * </pre>
 *
 * @author MimiMart Team
 * @since 1.0.0
 * @see com.mimimart.infrastructure.email.sender.SmtpEmailSender
 * @see com.mimimart.infrastructure.email.sender.ResendEmailSender
 * @see com.mimimart.infrastructure.email.sender.AwsSesEmailSender
 */
@Slf4j
@Configuration
public class EmailSenderConfig {

    @Value("${mimimart.email.provider:smtp}")
    private String emailProvider;

    @PostConstruct
    public void logEmailProvider() {
        log.info("===========================================");
        log.info("郵件發送模式: {}", emailProvider);
        log.info("===========================================");

        switch (emailProvider.toLowerCase()) {
            case "smtp":
                log.info("✓ 使用 SMTP 協定發送郵件 (SmtpEmailSender)");
                log.info("  適用場景: 開發環境、Mailpit、第三方 SMTP 服務");
                break;
            case "resend":
                log.info("✓ 使用 Resend API 發送郵件 (ResendEmailSender)");
                log.info("  適用場景: 生產環境、簡單易用、可靠穩定");
                break;
            case "aws-ses":
                log.info("✓ 使用 AWS SES SDK 發送郵件 (AwsSesEmailSender)");
                log.info("  適用場景: 生產環境、高併發、大量發送 (已廢棄)");
                break;
            default:
                log.warn("⚠ 未知的郵件發送模式: {}，將使用預設值 SMTP", emailProvider);
                break;
        }

        log.info("===========================================");
    }
}
