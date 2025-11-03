package com.mimimart.infrastructure.email.sender;

import jakarta.mail.MessagingException;

/**
 * 郵件發送策略介面
 *
 * <p>定義統一的郵件發送契約，支援多種發送方式：
 * <ul>
 *   <li>SMTP 發送 (開發環境/Mailpit)</li>
 *   <li>AWS SES SDK 發送 (生產環境)</li>
 * </ul>
 *
 * @author MimiMart Team
 * @since 1.0.0
 */
public interface EmailSender {

    /**
     * 發送 HTML 格式郵件
     *
     * @param to 收件人郵件地址
     * @param subject 郵件主旨
     * @param htmlContent HTML 格式的郵件內容
     * @throws MessagingException 當郵件發送失敗時拋出
     */
    void send(String to, String subject, String htmlContent) throws MessagingException;
}
