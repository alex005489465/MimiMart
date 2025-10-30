package com.mimimart.api.controller.storefront;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.member.AddressRequest;
import com.mimimart.api.dto.member.AddressResponse;
import com.mimimart.application.service.AddressService;
import com.mimimart.infrastructure.persistence.entity.MemberAddress;
import com.mimimart.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台收貨地址 Controller
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/storefront/address")
@Tag(name = "收貨地址", description = "會員收貨地址管理 API")
public class StorefrontAddressController {

    private final AddressService addressService;

    public StorefrontAddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * 查詢收貨地址列表
     */
    @GetMapping("/list")
    @Operation(summary = "查詢收貨地址列表", description = "取得當前會員的所有收貨地址")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddressList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<MemberAddress> addresses = addressService.getAddressList(userDetails.getUserId());

        List<AddressResponse> response = addresses.stream()
                .map(AddressResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("查詢成功", response));
    }

    /**
     * 新增收貨地址
     */
    @PostMapping("/create")
    @Operation(summary = "新增收貨地址", description = "新增一筆收貨地址")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {

        MemberAddress address = addressService.createAddress(
                userDetails.getUserId(),
                request.getRecipientName(),
                request.getPhone(),
                request.getAddress(),
                request.getIsDefault()
        );

        return ResponseEntity.ok(ApiResponse.success("新增成功", AddressResponse.from(address)));
    }

    /**
     * 更新收貨地址
     */
    @PostMapping("/{id}/update")
    @Operation(summary = "更新收貨地址", description = "更新指定的收貨地址")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {

        MemberAddress address = addressService.updateAddress(
                userDetails.getUserId(),
                id,
                request.getRecipientName(),
                request.getPhone(),
                request.getAddress()
        );

        return ResponseEntity.ok(ApiResponse.success("更新成功", AddressResponse.from(address)));
    }

    /**
     * 刪除收貨地址
     */
    @PostMapping("/{id}/delete")
    @Operation(summary = "刪除收貨地址", description = "刪除指定的收貨地址")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        addressService.deleteAddress(userDetails.getUserId(), id);

        return ResponseEntity.ok(ApiResponse.success("刪除成功"));
    }

    /**
     * 設為預設地址
     */
    @PostMapping("/{id}/set-default")
    @Operation(summary = "設為預設地址", description = "將指定的地址設為預設收貨地址")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        addressService.setDefaultAddress(userDetails.getUserId(), id);

        return ResponseEntity.ok(ApiResponse.success("設定成功"));
    }
}
