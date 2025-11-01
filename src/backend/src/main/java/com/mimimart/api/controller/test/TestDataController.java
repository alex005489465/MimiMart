package com.mimimart.api.controller.test;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.response.TestAdminAccountResponse;
import com.mimimart.api.dto.test.TestAccountResponse;
import com.mimimart.application.service.TestDataService;
import com.mimimart.infrastructure.persistence.entity.Admin;
import com.mimimart.infrastructure.persistence.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 測試資料 Controller
 * 提供測試環境所需的測試帳號管理端點
 *
 * 測試帳號規格:
 * - Email: test-member-001@test.com ~ test-member-100@test.com
 * - Password: password123 (所有帳號統一)
 * - Name: 測試會員001 ~ 測試會員100
 *
 * 注意:
 * - 僅在開發/測試環境啟用 (透過 app.test-endpoints.enabled=true)
 * - 生產環境會自動禁用此 Controller
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "測試資料", description = "測試環境專用的測試帳號管理 API (僅開發環境)")
@ConditionalOnProperty(name = "app.test-endpoints.enabled", havingValue = "true")
public class TestDataController {

    private final TestDataService testDataService;

    public TestDataController(TestDataService testDataService) {
        this.testDataService = testDataService;
    }

    /**
     * 獲取測試會員帳號
     */
    @GetMapping("/accounts/member")
    @Operation(
            summary = "獲取測試會員帳號",
            description = "返回指定數量的測試會員帳號資訊 (1-100個)。如果帳號不存在會自動創建。\n\n" +
                    "測試帳號規格:\n" +
                    "- Email: test-member-001@test.com ~ test-member-100@test.com\n" +
                    "- Password: password123 (統一密碼)\n" +
                    "- Name: 測試會員001 ~ 測試會員100"
    )
    public ResponseEntity<ApiResponse<List<TestAccountResponse>>> getTestMembers(
            @Parameter(description = "獲取的帳號數量", example = "1")
            @RequestParam(defaultValue = "1") int count) {

        // 獲取或創建測試會員
        List<Member> members = testDataService.getOrCreateTestMembers(count);

        // 組裝回應 (Controller 自己知道密碼是固定的)
        String password = testDataService.getDefaultPassword();
        List<TestAccountResponse> responses = members.stream()
                .map(member -> new TestAccountResponse(member.getEmail(), password))
                .collect(Collectors.toList());

        String message = String.format("成功獲取 %d 個測試帳號", count);
        return ResponseEntity.ok(ApiResponse.success(message, responses));
    }

    /**
     * 獲取測試管理員帳號
     */
    @GetMapping("/accounts/admin")
    @Operation(
            summary = "獲取測試管理員帳號",
            description = "返回指定數量的測試管理員帳號資訊 (1-100個)。如果帳號不存在會自動創建。\n\n" +
                    "測試帳號規格:\n" +
                    "- Username: test-admin-001 ~ test-admin-100\n" +
                    "- Email: test-admin-001@test.com ~ test-admin-100@test.com\n" +
                    "- Password: admin123 (統一密碼)\n" +
                    "- Name: 測試管理員001 ~ 測試管理員100"
    )
    public ResponseEntity<ApiResponse<List<TestAdminAccountResponse>>> getTestAdmins(
            @Parameter(description = "獲取的帳號數量", example = "1")
            @RequestParam(defaultValue = "1") int count) {

        // 獲取或創建測試管理員
        List<Admin> admins = testDataService.getOrCreateTestAdmins(count);

        // 組裝回應
        String password = testDataService.getAdminDefaultPassword();
        List<TestAdminAccountResponse> responses = admins.stream()
                .map(admin -> new TestAdminAccountResponse(
                        admin.getUsername(),
                        admin.getEmail(),
                        password
                ))
                .collect(Collectors.toList());

        String message = String.format("成功獲取 %d 個測試管理員帳號 (預設密碼: %s)", count, password);
        return ResponseEntity.ok(ApiResponse.success(message, responses));
    }

    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    @Operation(
            summary = "測試端點健康檢查",
            description = "確認測試端點是否正常運作"
    )
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("測試端點運作正常", "OK"));
    }
}
