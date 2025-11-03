package com.mimimart.application.service;

import com.mimimart.infrastructure.persistence.entity.EmailSendLog;
import com.mimimart.infrastructure.persistence.repository.EmailSendLogRepository;
import com.mimimart.shared.valueobject.EmailSendStatus;
import com.mimimart.shared.valueobject.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 郵件發送歷史記錄服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendLogService {

    private final EmailSendLogRepository emailSendLogRepository;

    /**
     * 記錄郵件發送成功
     *
     * @param memberId       會員 ID（可為 null）
     * @param recipientEmail 收件人郵箱
     * @param emailType      郵件類型
     * @param subject        郵件主旨
     */
    @Transactional
    public void logEmailSent(Long memberId, String recipientEmail, EmailType emailType, String subject) {
        EmailSendLog emailLog = new EmailSendLog();
        emailLog.setMemberId(memberId);
        emailLog.setRecipientEmail(recipientEmail);
        emailLog.setEmailType(emailType);
        emailLog.setSubject(subject);
        emailLog.setSentAt(LocalDateTime.now());
        emailLog.setStatus(EmailSendStatus.SUCCESS);

        emailSendLogRepository.save(emailLog);
        log.debug("郵件發送記錄已保存：會員 ID={}, 收件人={}, 類型={}", memberId, recipientEmail, emailType);
    }

    /**
     * 記錄郵件發送失敗
     *
     * @param memberId       會員 ID（可為 null）
     * @param recipientEmail 收件人郵箱
     * @param emailType      郵件類型
     * @param subject        郵件主旨
     * @param errorMessage   錯誤訊息
     */
    @Transactional
    public void logEmailFailed(Long memberId, String recipientEmail, EmailType emailType,
                               String subject, String errorMessage) {
        EmailSendLog emailLog = new EmailSendLog();
        emailLog.setMemberId(memberId);
        emailLog.setRecipientEmail(recipientEmail);
        emailLog.setEmailType(emailType);
        emailLog.setSubject(subject);
        emailLog.setSentAt(LocalDateTime.now());
        emailLog.setStatus(EmailSendStatus.FAILED);
        emailLog.setErrorMessage(errorMessage);

        emailSendLogRepository.save(emailLog);
        log.error("郵件發送失敗記錄已保存：會員 ID={}, 收件人={}, 類型={}, 錯誤={}",
                memberId, recipientEmail, emailType, errorMessage);
    }

    /**
     * 統計指定時間範圍內的發信總數
     *
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 發信總數
     */
    public long countEmailsSentBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return emailSendLogRepository.countBySentAtBetween(startTime, endTime);
    }

    /**
     * 統計會員在指定時間範圍內發送特定類型郵件的次數
     *
     * @param memberId  會員 ID
     * @param emailType 郵件類型
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 發信次數
     */
    public long countMemberEmailsSentBetween(Long memberId, EmailType emailType,
                                             LocalDateTime startTime, LocalDateTime endTime) {
        return emailSendLogRepository.countByMemberIdAndEmailTypeAndSentAtBetween(
                memberId, emailType, startTime, endTime);
    }
}
