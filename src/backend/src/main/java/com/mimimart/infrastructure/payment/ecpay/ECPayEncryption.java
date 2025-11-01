package com.mimimart.infrastructure.payment.ecpay;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 綠界金流加密工具
 * 負責產生 CheckMacValue(檢查碼)
 *
 * 加密流程:
 * 1. 參數依照 Key 值進行升冪排序
 * 2. 串接成 key1=value1&key2=value2... 格式
 * 3. 前後加上 HashKey 和 HashIV
 * 4. URL Encode(UTF-8)
 * 5. 轉小寫
 * 6. MD5 或 SHA256 加密
 * 7. 轉大寫
 *
 * @author MimiMart Development Team
 * @since 2.0.0 (ECPay Integration)
 */
@Component
public class ECPayEncryption {

    /**
     * 產生 CheckMacValue(使用 SHA256)
     *
     * @param params  要加密的參數(不包含 CheckMacValue)
     * @param hashKey 金鑰 HashKey
     * @param hashIv  金鑰 HashIV
     * @return CheckMacValue 檢查碼
     */
    public String generateCheckMacValue(Map<String, String> params, String hashKey, String hashIv) {
        // 1. 移除 CheckMacValue 參數(如果存在)
        Map<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("CheckMacValue");

        // 2. 依照 Key 值升冪排序並串接成字串
        String paramString = sortedParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // 3. 前後加上 HashKey 和 HashIV
        String rawString = "HashKey=" + hashKey + "&" + paramString + "&HashIV=" + hashIv;

        // 4. URL Encode(UTF-8)
        String encodedString = urlEncode(rawString);

        // 5. 轉小寫
        String lowercaseString = encodedString.toLowerCase();

        // 6. SHA256 加密
        String hash = sha256(lowercaseString);

        // 7. 轉大寫
        return hash.toUpperCase();
    }

    /**
     * 驗證 CheckMacValue
     *
     * @param params       收到的參數(包含 CheckMacValue)
     * @param hashKey      金鑰 HashKey
     * @param hashIv       金鑰 HashIV
     * @param receivedMac  收到的 CheckMacValue
     * @return true: 驗證通過, false: 驗證失敗
     */
    public boolean verifyCheckMacValue(Map<String, String> params, String hashKey, String hashIv, String receivedMac) {
        String calculatedMac = generateCheckMacValue(params, hashKey, hashIv);
        return calculatedMac.equals(receivedMac);
    }

    /**
     * URL Encode(UTF-8)
     * 特殊處理:將 %20 改為 +(符合綠界規範)
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8")
                    .replace("%2d", "-")
                    .replace("%5f", "_")
                    .replace("%2e", ".")
                    .replace("%21", "!")
                    .replace("%2a", "*")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%20", "+");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL Encode 失敗", e);
        }
    }

    /**
     * SHA256 加密
     */
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("SHA256 加密失敗", e);
        }
    }

    /**
     * Byte 陣列轉十六進位字串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
