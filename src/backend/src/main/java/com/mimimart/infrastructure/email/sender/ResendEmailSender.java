package com.mimimart.infrastructure.email.sender;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Resend 郵件發送實作
 *
 * <p>使用 Resend 官方 Java SDK 透過 REST API 發送郵件。
 * 適用於生產環境，提供簡單可靠的郵件發送服務。
 *
 * <p><b>優勢</b>：
 * <ul>
 *   <li>無需 SMTP 配置，使用 REST API</li>
 *   <li>每月 3,000 封免費額度</li>
 *   <li>無沙箱模式，立即生產可用</li>
 *   <li>簡單的 DNS 驗證流程</li>
 * </ul>
 *
 * @author MimiMart Team
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mimimart.email.provider", havingValue = "resend")
public class ResendEmailSender implements EmailSender {

    @Value("${mimimart.email.resend.api-key}")
    private String apiKey;

    @Value("${mimimart.email.from.address}")
    private String fromAddress;

    @Value("${mimimart.email.from.name:MimiMart}")
    private String fromName;

    private Resend resendClient;

    @PostConstruct
    public void init() {
        log.info("初始化 Resend Client: fromAddress={}", fromAddress);
        this.resendClient = new Resend(apiKey);
        log.info("Resend Client 初始化完成");
    }

    @Override
    public void send(String to, String subject, String htmlContent) throws MessagingException {
        log.debug("使用 Resend 發送郵件至: {}", to);

        try {
            // 設定寄件者（支援顯示名稱）
            String fromAddressWithName = String.format("%s <%s>", fromName, fromAddress);

            CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                .from(fromAddressWithName)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();

            CreateEmailResponse response = resendClient.emails().send(emailOptions);

            log.info("Resend 郵件發送成功: to={}, subject={}, id={}",
                to, subject, response.getId());

        } catch (ResendException e) {
            log.error("Resend 郵件發送失敗: to={}, error={}",
                to, e.getMessage(), e);
            throw new MessagingException("Resend 郵件發送失敗: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Resend 郵件發送發生未預期錯誤: to={}", to, e);
            throw new MessagingException("Resend 郵件發送失敗", e);
        }
    }
}
