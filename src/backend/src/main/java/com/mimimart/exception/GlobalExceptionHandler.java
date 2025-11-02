package com.mimimart.exception;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.domain.banner.exception.BannerNotFoundException;
import com.mimimart.domain.banner.exception.InvalidBannerOrderException;
import com.mimimart.domain.member.exception.*;
import com.mimimart.domain.order.exception.*;
import com.mimimart.shared.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域異常處理器
 * 符合專案 API 設計規範 (Constitution v1.2.1)
 *
 * 設計原則:
 * - Principle VII: 統一回應格式 (使用 ApiResponse)
 * - Principle VIII: 統一 HTTP 200 回應 + success 字段標示
 * - 業務異常透過 success: false 標示,基礎設施異常才回傳非 HTTP 200
 *
 * 日誌策略:
 * - 業務異常: WARN (預期內的錯誤,如會員不存在)
 * - 系統異常: ERROR + Stack Trace (非預期錯誤,需追蹤修復)
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 處理驗證錯誤異常
     * 業務層級錯誤,回傳 HTTP 200 + success: false
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("field", fieldError != null ? fieldError.getField() : "unknown");
        errorDetails.put("rejectedValue", fieldError != null && fieldError.getRejectedValue() != null
            ? fieldError.getRejectedValue().toString() : "null");

        String message = fieldError != null ? fieldError.getDefaultMessage() : "驗證錯誤";

        logger.warn("驗證錯誤: field={}, rejectedValue={}, message={}",
            errorDetails.get("field"), errorDetails.get("rejectedValue"), message);

        return ResponseEntity.ok(
            ApiResponse.error("VALIDATION_ERROR", message, errorDetails)
        );
    }

    /**
     * 處理會員已存在異常
     */
    @ExceptionHandler(MemberAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberAlreadyExists(MemberAlreadyExistsException ex) {
        logger.warn("會員已存在: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("MEMBER_ALREADY_EXISTS", ex.getMessage())
        );
    }

    /**
     * 處理無效憑證異常
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        logger.warn("無效憑證: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("INVALID_CREDENTIALS", ex.getMessage())
        );
    }

    /**
     * 處理帳號停用異常
     */
    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountDisabled(AccountDisabledException ex) {
        logger.warn("帳號已停用: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("ACCOUNT_DISABLED", ex.getMessage())
        );
    }

    /**
     * 處理會員不存在異常
     */
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberNotFound(MemberNotFoundException ex) {
        logger.warn("會員不存在: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("MEMBER_NOT_FOUND", ex.getMessage())
        );
    }

    /**
     * 處理 Email 已驗證異常
     */
    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyVerified(EmailAlreadyVerifiedException ex) {
        logger.warn("Email 已驗證: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("EMAIL_ALREADY_VERIFIED", "此 Email 已完成驗證")
        );
    }

    /**
     * 處理無效驗證 Token 異常
     */
    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidVerificationToken(InvalidVerificationTokenException ex) {
        logger.warn("無效驗證 Token: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("INVALID_TOKEN", ex.getMessage())
        );
    }

    /**
     * 處理驗證 Token 過期異常
     */
    @ExceptionHandler(VerificationTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpired(VerificationTokenExpiredException ex) {
        logger.warn("驗證 Token 已過期: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("TOKEN_EXPIRED", ex.getMessage())
        );
    }

    /**
     * 處理無效密碼重設 Token 異常
     */
    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidResetToken(InvalidResetTokenException ex) {
        logger.warn("無效密碼重設 Token: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("INVALID_RESET_TOKEN", ex.getMessage())
        );
    }

    /**
     * 處理密碼重設 Token 過期異常
     */
    @ExceptionHandler(ResetTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleResetTokenExpired(ResetTokenExpiredException ex) {
        logger.warn("密碼重設 Token 已過期: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("RESET_TOKEN_EXPIRED", ex.getMessage())
        );
    }

    /**
     * 處理密碼不一致異常
     */
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordMismatch(PasswordMismatchException ex) {
        logger.warn("密碼不一致: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("PASSWORD_MISMATCH", ex.getMessage())
        );
    }

    /**
     * 處理地址不存在異常
     */
    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAddressNotFound(AddressNotFoundException ex) {
        logger.warn("地址不存在: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("ADDRESS_NOT_FOUND", ex.getMessage())
        );
    }

    /**
     * 處理輪播圖不存在異常
     */
    @ExceptionHandler(BannerNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBannerNotFound(BannerNotFoundException ex) {
        logger.warn("輪播圖不存在: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("BANNER_NOT_FOUND", ex.getMessage())
        );
    }

    /**
     * 處理無效輪播圖順序異常
     */
    @ExceptionHandler(InvalidBannerOrderException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBannerOrder(InvalidBannerOrderException ex) {
        logger.warn("無效輪播圖順序: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("INVALID_BANNER_ORDER", ex.getMessage())
        );
    }

    /**
     * 處理訂單不存在異常
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(OrderNotFoundException ex) {
        logger.warn("訂單不存在: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("ORDER_NOT_FOUND", ex.getMessage())
        );
    }

    /**
     * 處理無效訂單狀態轉換異常
     */
    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOrderStatusTransition(InvalidOrderStatusTransitionException ex) {
        logger.warn("無效訂單狀態轉換: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("INVALID_ORDER_STATUS", ex.getMessage())
        );
    }

    /**
     * 處理購物車為空異常
     */
    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmptyCart(EmptyCartException ex) {
        logger.warn("購物車為空: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("EMPTY_CART", ex.getMessage())
        );
    }

    /**
     * 處理未授權訂單存取異常
     */
    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedOrderAccess(UnauthorizedOrderAccessException ex) {
        logger.warn("未授權訂單存取: {}", ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error("UNAUTHORIZED_ORDER_ACCESS", ex.getMessage())
        );
    }

    /**
     * 處理領域異常基礎類別
     * 捕捉所有未被明確處理的 DomainException 子類別
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        String errorCode = ex.getErrorCode() != null ? ex.getErrorCode() : "DOMAIN_ERROR";
        logger.warn("領域異常: code={}, message={}", errorCode, ex.getMessage());
        return ResponseEntity.ok(
            ApiResponse.error(errorCode, ex.getMessage())
        );
    }

    /**
     * 處理通用異常
     * 記錄完整異常堆疊,回傳 HTTP 200 + ApiResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        logger.error("系統異常: URI={}, Error={}", requestURI, ex.getMessage(), ex);
        return ResponseEntity.ok(
            ApiResponse.error("INTERNAL_SERVER_ERROR", "系統發生錯誤,請稍後再試")
        );
    }
}
