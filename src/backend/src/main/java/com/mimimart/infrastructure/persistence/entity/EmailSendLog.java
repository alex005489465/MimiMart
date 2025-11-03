package com.mimimart.infrastructure.persistence.entity;

import com.mimimart.shared.valueobject.EmailType;
import com.mimimart.shared.valueobject.EmailSendStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 郵件發送歷史記錄 Entity
 * 對應 email_send_log 資料表
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "email_send_log", indexes = {
    @Index(name = "idx_member_id", columnList = "member_id"),
    @Index(name = "idx_recipient_email", columnList = "recipient_email"),
    @Index(name = "idx_email_type", columnList = "email_type"),
    @Index(name = "idx_sent_at", columnList = "sent_at"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailSendStatus status = EmailSendStatus.SUCCESS;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
