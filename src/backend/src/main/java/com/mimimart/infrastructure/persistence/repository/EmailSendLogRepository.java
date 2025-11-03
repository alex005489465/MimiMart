package com.mimimart.infrastructure.persistence.repository;

import com.mimimart.infrastructure.persistence.entity.EmailSendLog;
import com.mimimart.shared.valueobject.EmailType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 郵件發送歷史記錄 Repository
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Repository
public interface EmailSendLogRepository extends JpaRepository<EmailSendLog, Long> {

    /**
     * 統計指定時間範圍內的發信總數
     *
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 發信總數
     */
    @Query("SELECT COUNT(e) FROM EmailSendLog e WHERE e.sentAt >= :startTime AND e.sentAt < :endTime")
    long countBySentAtBetween(@Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    /**
     * 統計會員在指定時間範圍內發送特定類型郵件的次數
     *
     * @param memberId  會員 ID
     * @param emailType 郵件類型
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 發信次數
     */
    @Query("SELECT COUNT(e) FROM EmailSendLog e WHERE e.memberId = :memberId " +
           "AND e.emailType = :emailType AND e.sentAt >= :startTime AND e.sentAt < :endTime")
    long countByMemberIdAndEmailTypeAndSentAtBetween(
            @Param("memberId") Long memberId,
            @Param("emailType") EmailType emailType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
