package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.member.ChangePasswordRequest;
import com.mimimart.api.dto.member.MemberProfile;
import com.mimimart.api.dto.member.UpdateProfileRequest;
import com.mimimart.application.service.MemberService;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 前台會員資料 Controller
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/shop/member")
@Tag(name = "會員資料", description = "會員個人資料管理 API")
public class ShopMemberController {

    private final MemberService memberService;

    public ShopMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 查看個人資料
     */
    @GetMapping("/profile")
    @Operation(summary = "查看個人資料", description = "取得當前登入會員的個人資料")
    public ResponseEntity<ApiResponse<MemberProfile>> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberService.getProfile(userDetails.getUserId());

        MemberProfile profile = new MemberProfile(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getHomeAddress(),
                member.getEmailVerified()
        );

        return ResponseEntity.ok(ApiResponse.success("查詢成功", profile));
    }

    /**
     * 更新個人資料
     */
    @PostMapping("/profile/update")
    @Operation(summary = "更新個人資料", description = "更新會員的姓名、電話、住家地址")
    public ResponseEntity<ApiResponse<MemberProfile>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {

        Member member = memberService.updateProfile(
                userDetails.getUserId(),
                request.getName(),
                request.getPhone(),
                request.getHomeAddress()
        );

        MemberProfile profile = new MemberProfile(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getHomeAddress(),
                member.getEmailVerified()
        );

        return ResponseEntity.ok(ApiResponse.success("更新成功", profile));
    }

    /**
     * 修改密碼
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密碼", description = "修改會員登入密碼")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        memberService.changePassword(
                userDetails.getUserId(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(ApiResponse.success("密碼修改成功"));
    }
}
