package com.mimimart.application.service;

/**
 * 郵件服務介面
 */
public interface EmailService {

    /**
     * 發送歡迎郵件
     *
     * @param email 收件人 Email
     * @param memberName 會員名稱
     */
    void sendWelcomeEmail(String email, String memberName);

    /**
     * 發送 Email 驗證郵件
     *
     * @param email 收件人 Email
     * @param memberName 會員名稱
     * @param verificationToken 驗證 Token
     */
    void sendVerificationEmail(String email, String memberName, String verificationToken);

    /**
     * 發送密碼重設郵件
     *
     * @param email 收件人 Email
     * @param memberName 會員名稱
     * @param resetToken 密碼重設 Token
     */
    void sendPasswordResetEmail(String email, String memberName, String resetToken);
}
