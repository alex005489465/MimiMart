package com.mimimart.api.dto.member;

import com.mimimart.shared.valueobject.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 管理員查詢會員列表請求 DTO
 * 支援搜尋、篩選、排序功能
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminMemberQueryRequest {

    /**
     * 搜尋關鍵字（支援 Email、姓名、電話、會員 ID）
     */
    private String keyword;

    /**
     * 帳號狀態篩選
     */
    private MemberStatus status;

    /**
     * 註冊開始日期（含）
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    /**
     * 註冊結束日期（含）
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    /**
     * 排序欄位（id, createdAt, lastLoginAt）
     * 預設：createdAt
     */
    private String sortBy = "createdAt";

    /**
     * 排序方向（asc, desc）
     * 預設：desc
     */
    private String sortOrder = "desc";
}
