package com.mimimart.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 統一 API 回應格式
 * 符合專案 API 設計規範 (Constitution v1.2.1)
 *
 * @param <T> 資料類型
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {

    private Boolean success;
    private String code;
    private String message;
    private T data;
    private Object meta;

    // Custom Constructors
    public ApiResponse(Boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(Boolean success, String code, String message, T data, Object meta) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.meta = meta;
    }

    // Static factory methods for common responses

    /**
     * 成功回應 (無資料)
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, "SUCCESS", message, null);
    }

    /**
     * 成功回應 (含資料)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data);
    }

    /**
     * 成功回應 (含資料與 meta)
     */
    public static <T> ApiResponse<T> success(String message, T data, Object meta) {
        return new ApiResponse<>(true, "SUCCESS", message, data, meta);
    }

    /**
     * 失敗回應
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    /**
     * 失敗回應 (含資料)
     */
    public static <T> ApiResponse<T> error(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}
