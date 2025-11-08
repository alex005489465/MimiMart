package com.mimimart.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 會員購物車主表實體
 * 記錄購物車元數據,實際項目儲存在 Redis
 * 零約束設計：資料完整性由應用程式層驗證
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "member_carts")
@Getter
@Setter
@NoArgsConstructor
public class MemberCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "max_items_count", nullable = false)
    private Integer maxItemsCount = 100;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Custom Constructor
    public MemberCart(Long memberId) {
        this.memberId = memberId;
        this.status = "ACTIVE";
        this.maxItemsCount = 100;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
