package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.member.*;
import com.mimimart.application.service.AddressService;
import com.mimimart.application.service.MemberService;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.entity.MemberAddress;
import com.mimimart.shared.valueobject.MemberStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 後台會員管理 Controller
 */
@RestController
@RequestMapping("/api/admin/member")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "後台 - 會員管理", description = "後台會員管理 API")
public class AdminMemberController {

    private final MemberService memberService;
    private final AddressService addressService;

    /**
     * 查詢會員列表
     */
    @GetMapping("/list")
    @Operation(summary = "查詢會員列表", description = "查詢所有會員，支援搜尋、篩選、排序和分頁")
    public ResponseEntity<ApiResponse<List<MemberListItemResponse>>> getMemberList(
            @ModelAttribute AdminMemberQueryRequest queryRequest,
            @Parameter(description = "頁碼 (從 1 開始)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size) {

        // 驗證日期範圍
        if (queryRequest.getStartDate() != null && queryRequest.getEndDate() != null) {
            if (queryRequest.getStartDate().isAfter(queryRequest.getEndDate())) {
                return ResponseEntity.ok(ApiResponse.error("INVALID_DATE_RANGE", "開始日期不可晚於結束日期"));
            }
        }

        // 驗證排序欄位
        String sortBy = queryRequest.getSortBy();
        if (!isValidSortField(sortBy)) {
            sortBy = "createdAt"; // 使用預設排序欄位
        }

        // 驗證排序方向
        String sortOrder = queryRequest.getSortOrder();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // 將前端的 1-based 頁碼轉換為 Spring Data JPA 的 0-based
        int zeroBasedPage = page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size, Sort.by(direction, sortBy));

        // 查詢會員列表
        Page<Member> memberPage = memberService.getMemberListForAdmin(
                queryRequest.getKeyword(),
                queryRequest.getStatus(),
                queryRequest.getStartDate(),
                queryRequest.getEndDate(),
                pageable
        );

        // 轉換為 DTO
        List<MemberListItemResponse> response = memberPage.getContent().stream()
                .map(MemberListItemResponse::from)
                .collect(Collectors.toList());

        // 建立分頁 meta 資訊
        Map<String, Object> meta = new HashMap<>();
        meta.put("currentPage", memberPage.getNumber() + 1);  // 轉回 1-based
        meta.put("totalPages", memberPage.getTotalPages());
        meta.put("totalItems", memberPage.getTotalElements());
        meta.put("pageSize", memberPage.getSize());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response, meta));
    }

    /**
     * 查詢會員詳細資料
     */
    @GetMapping("/detail")
    @Operation(summary = "查詢會員詳細資料", description = "查詢指定會員的完整資料，包含收貨地址列表")
    public ResponseEntity<ApiResponse<MemberDetailResponse>> getMemberDetail(
            @Parameter(description = "會員 ID", required = true) @RequestParam Long memberId) {

        // 查詢會員基本資料
        Member member = memberService.getMemberDetailForAdmin(memberId);

        // 查詢會員的收貨地址列表
        List<MemberAddress> addresses = addressService.getAddressList(memberId);
        List<MemberAddressItemResponse> addressResponses = addresses.stream()
                .map(MemberAddressItemResponse::from)
                .collect(Collectors.toList());

        // 組合回應資料
        MemberDetailResponse response = MemberDetailResponse.from(member, addressResponses);

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    /**
     * 驗證排序欄位是否有效
     */
    private boolean isValidSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return false;
        }
        // 允許的排序欄位
        return sortBy.equals("id")
                || sortBy.equals("createdAt")
                || sortBy.equals("lastLoginAt");
    }
}
