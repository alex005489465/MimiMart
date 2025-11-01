package com.mimimart.api.controller.shop;

import com.mimimart.application.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 綠界金流回調控制器
 * 處理綠界付款結果通知
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (ECPay Integration)
 */
@RestController
@RequestMapping("/api/shop/payment/ecpay")
public class ECPayCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(ECPayCallbackController.class);

    private final PaymentService paymentService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public ECPayCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 測試端點 - 驗證外部訪問是否正常
     *
     * @return 簡單的回應訊息
     */
    @GetMapping(value = "/up", produces = MediaType.TEXT_PLAIN_VALUE)
    public String healthCheck() {
        logger.info("收到 /api/shop/payment/ecpay/up 請求");
        return "ECPay endpoint is UP! Time: " + java.time.Instant.now();
    }

    /**
     * 處理綠界付款結果回調
     * 綠界會以 POST 方式傳送付款結果到此端點
     *
     * 綠界規範:
     * - HTTP Method: POST
     * - Content-Type: application/x-www-form-urlencoded
     * - Accept: text/html
     * - 回應格式: 純文字 "1|OK"
     *
     * @param params 綠界回傳的參數
     * @return 必須回傳 "1|OK" 告知綠界已收到通知
     */
    @PostMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    public String handleCallback(@RequestParam Map<String, String> params) {
        logger.info("收到綠界付款回調: {}", params);

        try {
            // 委託給 PaymentService 處理
            paymentService.handlePaymentCallback(params);

            // 回傳成功給綠界
            return "1|OK";

        } catch (Exception e) {
            logger.error("處理綠界回調時發生錯誤", e);
            return "0|處理失敗: " + e.getMessage();
        }
    }

    /**
     * 處理綠界付款完成返回頁面
     * 使用者付款完成後會跳轉到此頁面
     *
     * 實作倒數跳轉功能:
     * - 顯示付款結果
     * - 5秒倒數計時
     * - 自動跳轉到前端頁面
     * - 提供立即跳轉按鈕
     *
     * @param params 綠界回傳的參數
     * @return HTML 頁面(含倒數跳轉邏輯)
     */
    @PostMapping(value = "/return", produces = MediaType.TEXT_HTML_VALUE)
    public String handleReturn(@RequestParam Map<String, String> params) {
        logger.info("使用者從綠界返回: {}", params);

        try {
            // 解析回調資訊
            String rtnCode = params.get("RtnCode");
            String merchantTradeNo = params.get("MerchantTradeNo");
            String tradeNo = params.get("TradeNo");
            String tradeAmt = params.get("TradeAmt");
            String tradeDate = params.get("TradeDate");
            String rtnMsg = params.get("RtnMsg");

            boolean isSuccess = "1".equals(rtnCode);

            // 產生倒數跳轉頁面
            if (isSuccess) {
                return generateSuccessPageWithRedirect(merchantTradeNo, tradeNo, tradeAmt, tradeDate);
            } else {
                return generateFailurePageWithRedirect(rtnMsg);
            }

        } catch (Exception e) {
            logger.error("處理綠界返回頁面時發生錯誤", e);
            return generateErrorPage(e.getMessage());
        }
    }

    /**
     * 產生付款成功頁面(含倒數跳轉)
     */
    private String generateSuccessPageWithRedirect(String merchantTradeNo, String tradeNo, String tradeAmt, String tradeDate) {
        // 建立前端回調 URL (帶參數)
        String callbackUrl = String.format(
            "%s/payment/callback?success=true&paymentNumber=%s&tradeNo=%s&amount=%s",
            frontendUrl,
            merchantTradeNo != null ? merchantTradeNo : "",
            tradeNo != null ? tradeNo : "",
            tradeAmt != null ? tradeAmt : ""
        );

        return """
                <!DOCTYPE html>
                <html lang='zh-TW'>
                <head>
                    <meta charset='UTF-8'>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>付款成功</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft JhengHei', Arial, sans-serif;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 20px;
                        }
                        .container {
                            background: white;
                            padding: 40px;
                            border-radius: 16px;
                            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                            max-width: 500px;
                            width: 100%%;
                            text-align: center;
                        }
                        .success-icon {
                            width: 80px;
                            height: 80px;
                            background: #28a745;
                            border-radius: 50%%;
                            margin: 0 auto 20px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            animation: scaleIn 0.5s ease-out;
                        }
                        .success-icon::before {
                            content: '✓';
                            color: white;
                            font-size: 48px;
                            font-weight: bold;
                        }
                        @keyframes scaleIn {
                            from {
                                transform: scale(0);
                            }
                            to {
                                transform: scale(1);
                            }
                        }
                        h1 {
                            color: #28a745;
                            font-size: 28px;
                            margin-bottom: 10px;
                        }
                        .info {
                            background: #f8f9fa;
                            padding: 20px;
                            border-radius: 8px;
                            margin: 20px 0;
                            text-align: left;
                        }
                        .info-item {
                            margin: 12px 0;
                            color: #495057;
                            font-size: 14px;
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                        }
                        .info-label {
                            font-weight: 600;
                            color: #6c757d;
                        }
                        .info-value {
                            font-weight: 500;
                            color: #212529;
                        }
                        .countdown {
                            font-size: 18px;
                            color: #6c757d;
                            margin: 20px 0;
                        }
                        .countdown-number {
                            display: inline-block;
                            background: #667eea;
                            color: white;
                            width: 40px;
                            height: 40px;
                            line-height: 40px;
                            border-radius: 50%%;
                            font-weight: bold;
                            font-size: 24px;
                            margin: 0 8px;
                        }
                        .btn {
                            background: #667eea;
                            color: white;
                            border: none;
                            padding: 14px 32px;
                            border-radius: 8px;
                            font-size: 16px;
                            font-weight: 600;
                            cursor: pointer;
                            transition: all 0.3s ease;
                            margin-top: 10px;
                        }
                        .btn:hover {
                            background: #5568d3;
                            transform: translateY(-2px);
                            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
                        }
                        .btn:active {
                            transform: translateY(0);
                        }
                    </style>
                </head>
                <body>
                    <div class='container'>
                        <div class='success-icon'></div>
                        <h1>付款成功!</h1>
                        <p style='color: #6c757d; margin-bottom: 20px;'>感謝您的購買</p>

                        <div class='info'>
                            <div class='info-item'>
                                <span class='info-label'>付款編號</span>
                                <span class='info-value'>%s</span>
                            </div>
                            <div class='info-item'>
                                <span class='info-label'>交易編號</span>
                                <span class='info-value'>%s</span>
                            </div>
                            <div class='info-item'>
                                <span class='info-label'>付款金額</span>
                                <span class='info-value'>NT$ %s</span>
                            </div>
                            <div class='info-item'>
                                <span class='info-label'>付款時間</span>
                                <span class='info-value'>%s</span>
                            </div>
                        </div>

                        <div class='countdown'>
                            <span class='countdown-number' id='seconds'>5</span>
                            <span>秒後自動跳轉...</span>
                        </div>

                        <button class='btn' onclick='redirectNow()'>立即跳轉到訂單頁面</button>
                    </div>

                    <script>
                        const callbackUrl = '%s';
                        let countdown = 5;
                        const countdownElement = document.getElementById('seconds');

                        function redirectNow() {
                            window.location.href = callbackUrl;
                        }

                        const timer = setInterval(() => {
                            countdown--;
                            countdownElement.textContent = countdown;

                            if (countdown <= 0) {
                                clearInterval(timer);
                                redirectNow();
                            }
                        }, 1000);
                    </script>
                </body>
                </html>
                """.formatted(
                merchantTradeNo != null ? merchantTradeNo : "N/A",
                tradeNo != null ? tradeNo : "N/A",
                tradeAmt != null ? tradeAmt : "0",
                tradeDate != null ? tradeDate : "N/A",
                callbackUrl
        );
    }

    /**
     * 產生付款失敗頁面(含倒數跳轉)
     */
    private String generateFailurePageWithRedirect(String rtnMsg) {
        // 建立前端回調 URL (帶參數)
        String callbackUrl = String.format(
            "%s/payment/callback?success=false&message=%s",
            frontendUrl,
            rtnMsg != null ? java.net.URLEncoder.encode(rtnMsg, java.nio.charset.StandardCharsets.UTF_8) : ""
        );

        return """
                <!DOCTYPE html>
                <html lang='zh-TW'>
                <head>
                    <meta charset='UTF-8'>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>付款失敗</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft JhengHei', Arial, sans-serif;
                            background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 20px;
                        }
                        .container {
                            background: white;
                            padding: 40px;
                            border-radius: 16px;
                            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                            max-width: 500px;
                            width: 100%%;
                            text-align: center;
                        }
                        .failure-icon {
                            width: 80px;
                            height: 80px;
                            background: #dc3545;
                            border-radius: 50%%;
                            margin: 0 auto 20px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            animation: scaleIn 0.5s ease-out;
                        }
                        .failure-icon::before {
                            content: '✗';
                            color: white;
                            font-size: 48px;
                            font-weight: bold;
                        }
                        @keyframes scaleIn {
                            from {
                                transform: scale(0);
                            }
                            to {
                                transform: scale(1);
                            }
                        }
                        h1 {
                            color: #dc3545;
                            font-size: 28px;
                            margin-bottom: 20px;
                        }
                        .message {
                            background: #f8d7da;
                            color: #721c24;
                            padding: 16px;
                            border-radius: 8px;
                            margin: 20px 0;
                            font-size: 14px;
                        }
                        .countdown {
                            font-size: 18px;
                            color: #6c757d;
                            margin: 20px 0;
                        }
                        .countdown-number {
                            display: inline-block;
                            background: #dc3545;
                            color: white;
                            width: 40px;
                            height: 40px;
                            line-height: 40px;
                            border-radius: 50%%;
                            font-weight: bold;
                            font-size: 24px;
                            margin: 0 8px;
                        }
                        .btn {
                            background: #dc3545;
                            color: white;
                            border: none;
                            padding: 14px 32px;
                            border-radius: 8px;
                            font-size: 16px;
                            font-weight: 600;
                            cursor: pointer;
                            transition: all 0.3s ease;
                            margin-top: 10px;
                        }
                        .btn:hover {
                            background: #c82333;
                            transform: translateY(-2px);
                            box-shadow: 0 4px 12px rgba(220, 53, 69, 0.4);
                        }
                    </style>
                </head>
                <body>
                    <div class='container'>
                        <div class='failure-icon'></div>
                        <h1>付款失敗</h1>

                        <div class='message'>
                            <strong>錯誤訊息:</strong><br>
                            %s
                        </div>

                        <p style='color: #6c757d; margin: 20px 0;'>請稍後再試或聯繫客服</p>

                        <div class='countdown'>
                            <span class='countdown-number' id='seconds'>5</span>
                            <span>秒後自動返回...</span>
                        </div>

                        <button class='btn' onclick='redirectNow()'>立即返回</button>
                    </div>

                    <script>
                        const callbackUrl = '%s';
                        let countdown = 5;
                        const countdownElement = document.getElementById('seconds');

                        function redirectNow() {
                            window.location.href = callbackUrl;
                        }

                        const timer = setInterval(() => {
                            countdown--;
                            countdownElement.textContent = countdown;

                            if (countdown <= 0) {
                                clearInterval(timer);
                                redirectNow();
                            }
                        }, 1000);
                    </script>
                </body>
                </html>
                """.formatted(rtnMsg != null ? rtnMsg : "未知錯誤", callbackUrl);
    }

    /**
     * 產生錯誤頁面
     */
    private String generateErrorPage(String errorMessage) {
        return """
                <!DOCTYPE html>
                <html lang='zh-TW'>
                <head>
                    <meta charset='UTF-8'>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>系統錯誤</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            text-align: center;
                            padding: 50px;
                            background: #f0f0f0;
                        }
                        .container {
                            background: white;
                            padding: 30px;
                            border-radius: 10px;
                            max-width: 500px;
                            margin: 0 auto;
                        }
                        .error {
                            color: #dc3545;
                            font-size: 24px;
                            font-weight: bold;
                        }
                    </style>
                </head>
                <body>
                    <div class='container'>
                        <div class='error'>系統錯誤</div>
                        <p>%s</p>
                    </div>
                </body>
                </html>
                """.formatted(errorMessage);
    }
}
