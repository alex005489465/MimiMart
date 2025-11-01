package com.mimimart.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 綠界金流配置
 * 從 application.yml 讀取綠界 API 相關設定
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (ECPay Integration)
 */
@Configuration
@ConfigurationProperties(prefix = "mimimart.ecpay")
public class ECPayConfig {

    /**
     * 特店編號(測試環境: 2000132)
     */
    private String merchantId;

    /**
     * 金鑰 HashKey(用於產生檢查碼)
     */
    private String hashKey;

    /**
     * 金鑰 HashIV(用於產生檢查碼)
     */
    private String hashIv;

    /**
     * API 基礎網址
     * 測試環境: https://payment-stage.ecpay.com.tw
     * 正式環境: https://payment.ecpay.com.tw
     */
    private String apiUrl;

    /**
     * ClientBackURL - 使用者付款完成後返回的頁面 URL
     * 對應綠界參數: ClientBackURL
     * 使用者在綠界頁面付款完成後,會看到「返回商店」按鈕,點擊後跳轉到此 URL
     */
    private String returnUrl;

    /**
     * ReturnURL - 伺服器端接收付款結果通知的 URL
     * 對應綠界參數: ReturnURL
     * 綠界會以 POST 方式將付款結果通知到此 URL(Server 端)
     */
    private String callbackUrl;

    // Getters and Setters

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getHashIv() {
        return hashIv;
    }

    public void setHashIv(String hashIv) {
        this.hashIv = hashIv;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    /**
     * 取得建立付款訂單的完整 API URL
     */
    public String getCreatePaymentUrl() {
        return apiUrl + "/Cashier/AioCheckOut/V5";
    }
}
