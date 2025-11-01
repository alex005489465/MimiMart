package com.mimimart.infrastructure.payment.ecpay;

import com.mimimart.domain.order.model.Money;
import com.mimimart.domain.payment.model.Payment;
import com.mimimart.infrastructure.config.ECPayConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 綠界金流服務
 * 負責與綠界 API 整合,處理付款請求與回調
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (ECPay Integration)
 */
@Service
public class ECPayService {

    private final ECPayConfig ecPayConfig;
    private final ECPayEncryption ecPayEncryption;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public ECPayService(ECPayConfig ecPayConfig, ECPayEncryption ecPayEncryption) {
        this.ecPayConfig = ecPayConfig;
        this.ecPayEncryption = ecPayEncryption;
    }

    /**
     * 建立綠界付款訂單參數
     *
     * @param payment 付款領域模型
     * @param itemDescription 商品描述
     * @param paymentType 付款方式 (Credit, ATM, CVS, BARCODE)
     * @return 綠界 API 參數(包含 CheckMacValue)
     */
    public Map<String, String> createPaymentParams(Payment payment, String itemDescription, String paymentType) {
        Map<String, String> params = new HashMap<>();

        // === 基本參數 ===
        params.put("MerchantID", ecPayConfig.getMerchantId());
        params.put("MerchantTradeNo", payment.getPaymentNumber().getValue()); // 使用付款編號作為特店交易編號
        params.put("MerchantTradeDate", formatDateTime(LocalDateTime.now()));
        params.put("PaymentType", "aio"); // 固定值: aio (All In One)
        params.put("TotalAmount", formatAmount(payment.getAmount())); // 交易金額(整數)
        params.put("TradeDesc", "MimiMart 商品購買"); // 交易描述
        params.put("ItemName", itemDescription); // 商品名稱
        params.put("ReturnURL", ecPayConfig.getCallbackUrl()); // 綠界參數 ReturnURL: Server 端接收付款結果通知
        params.put("ClientBackURL", ecPayConfig.getReturnUrl()); // 綠界參數 ClientBackURL: 使用者返回商店按鈕

        // === 付款方式 ===
        params.put("ChoosePayment", paymentType); // Credit, ATM, CVS, BARCODE

        // === 額外參數 ===
        params.put("EncryptType", "1"); // 固定值: 1 (SHA256)
        params.put("NeedExtraPaidInfo", "N"); // 是否需要額外付款資訊

        // === 產生檢查碼 ===
        String checkMacValue = ecPayEncryption.generateCheckMacValue(
                params,
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv()
        );
        params.put("CheckMacValue", checkMacValue);

        return params;
    }

    /**
     * 驗證綠界回調參數
     *
     * @param callbackParams 回調參數
     * @return true: 驗證通過, false: 驗證失敗
     */
    public boolean verifyCallback(Map<String, String> callbackParams) {
        String receivedMac = callbackParams.get("CheckMacValue");
        if (receivedMac == null) {
            return false;
        }

        return ecPayEncryption.verifyCheckMacValue(
                callbackParams,
                ecPayConfig.getHashKey(),
                ecPayConfig.getHashIv(),
                receivedMac
        );
    }

    /**
     * 解析綠界回調參數
     *
     * @param callbackParams 回調參數
     * @return 回調資訊物件
     */
    public ECPayCallbackInfo parseCallback(Map<String, String> callbackParams) {
        ECPayCallbackInfo info = new ECPayCallbackInfo();
        info.setMerchantTradeNo(callbackParams.get("MerchantTradeNo")); // 特店交易編號(對應我們的 PaymentNumber)
        info.setTradeNo(callbackParams.get("TradeNo")); // 綠界交易編號
        info.setRtnCode(callbackParams.get("RtnCode")); // 交易狀態(1: 成功)
        info.setRtnMsg(callbackParams.get("RtnMsg")); // 交易訊息
        info.setTradeAmt(callbackParams.get("TradeAmt")); // 交易金額
        info.setPaymentType(callbackParams.get("PaymentType")); // 付款方式
        info.setPaymentDate(callbackParams.get("PaymentDate")); // 付款時間 (yyyy/MM/dd HH:mm:ss)
        info.setTradeDate(callbackParams.get("TradeDate")); // 訂單成立時間
        return info;
    }

    /**
     * 檢查付款是否成功
     *
     * @param rtnCode 綠界回傳的狀態碼
     * @return true: 付款成功
     */
    public boolean isPaymentSuccess(String rtnCode) {
        return "1".equals(rtnCode); // 綠界文件:1 表示付款成功
    }

    // ========================================
    // 私有輔助方法
    // ========================================

    /**
     * 格式化日期時間為綠界要求的格式: yyyy/MM/dd HH:mm:ss
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化金額(移除小數點,轉為整數字串)
     * 綠界要求:交易金額必須為整數
     */
    private String formatAmount(Money amount) {
        return amount.getAmount().setScale(0, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 綠界回調資訊物件
     */
    public static class ECPayCallbackInfo {
        private String merchantTradeNo; // 特店交易編號(PaymentNumber)
        private String tradeNo; // 綠界交易編號
        private String rtnCode; // 交易狀態碼
        private String rtnMsg; // 交易訊息
        private String tradeAmt; // 交易金額
        private String paymentType; // 付款方式
        private String paymentDate; // 付款時間 (yyyy/MM/dd HH:mm:ss)
        private String tradeDate; // 訂單成立時間

        // Getters and Setters

        public String getMerchantTradeNo() {
            return merchantTradeNo;
        }

        public void setMerchantTradeNo(String merchantTradeNo) {
            this.merchantTradeNo = merchantTradeNo;
        }

        public String getTradeNo() {
            return tradeNo;
        }

        public void setTradeNo(String tradeNo) {
            this.tradeNo = tradeNo;
        }

        public String getRtnCode() {
            return rtnCode;
        }

        public void setRtnCode(String rtnCode) {
            this.rtnCode = rtnCode;
        }

        public String getRtnMsg() {
            return rtnMsg;
        }

        public void setRtnMsg(String rtnMsg) {
            this.rtnMsg = rtnMsg;
        }

        public String getTradeAmt() {
            return tradeAmt;
        }

        public void setTradeAmt(String tradeAmt) {
            this.tradeAmt = tradeAmt;
        }

        public String getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(String paymentType) {
            this.paymentType = paymentType;
        }

        public String getPaymentDate() {
            return paymentDate;
        }

        public void setPaymentDate(String paymentDate) {
            this.paymentDate = paymentDate;
        }

        public String getTradeDate() {
            return tradeDate;
        }

        public void setTradeDate(String tradeDate) {
            this.tradeDate = tradeDate;
        }
    }
}
