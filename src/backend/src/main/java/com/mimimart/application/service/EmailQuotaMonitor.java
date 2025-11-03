package com.mimimart.application.service;

import com.mimimart.infrastructure.persistence.entity.EmailQuotaConfig;
import com.mimimart.infrastructure.persistence.repository.EmailQuotaConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 郵件配額監控服務
 * 定期檢查配額使用率並發送告警
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQuotaMonitor {

    private final EmailQuotaService emailQuotaService;
    private final EmailQuotaConfigRepository emailQuotaConfigRepository;

    @Value("${mimimart.email.quota.alert-thresholds}")
    private String alertThresholds;

    /**
     * 每小時檢查一次配額使用率
     * 當達到預設閾值（80%、90%、100%）時發送告警
     */
    @Scheduled(cron = "0 0 * * * ?")  // 每小時整點執行
    public void checkQuotaUsage() {
        try {
            double usagePercentage = emailQuotaService.getCurrentMonthUsagePercentage();
            long currentUsage = emailQuotaService.getCurrentMonthUsage();

            log.debug("郵件配額使用率檢查：{}%", String.format("%.2f", usagePercentage));

            // 解析告警閾值
            String[] thresholds = alertThresholds.split(",");
            for (String thresholdStr : thresholds) {
                int threshold = Integer.parseInt(thresholdStr.trim());

                if (usagePercentage >= threshold) {
                    // 檢查是否已發送過該閾值的告警
                    String configKey = "alert_threshold_" + threshold;
                    Optional<EmailQuotaConfig> config = emailQuotaConfigRepository.findByConfigKey(configKey);

                    boolean alreadySent = config.isPresent() && "true".equals(config.get().getConfigValue());

                    if (!alreadySent) {
                        // 發送告警
                        sendAlert(threshold, usagePercentage, currentUsage);

                        // 標記已發送
                        markAlertAsSent(configKey);
                    }
                }
            }
        } catch (Exception e) {
            log.error("郵件配額監控檢查失敗", e);
        }
    }

    /**
     * 發送告警通知
     *
     * @param threshold        閾值
     * @param usagePercentage  使用率
     * @param currentUsage     當前使用量
     */
    private void sendAlert(int threshold, double usagePercentage, long currentUsage) {
        String message = String.format(
                "【郵件配額告警】當月郵件發送量已達 %d%% 閾值！目前使用率：%.2f%%，已發送：%d 封",
                threshold, usagePercentage, currentUsage
        );

        log.warn(message);

        // TODO: 這裡可以整合其他告警方式
        // 例如：發送郵件給管理員、發送 Slack 通知、發送簡訊等
        // 由於發送郵件會消耗配額，建議使用其他通知方式
    }

    /**
     * 標記告警已發送
     *
     * @param configKey 配置鍵
     */
    private void markAlertAsSent(String configKey) {
        Optional<EmailQuotaConfig> config = emailQuotaConfigRepository.findByConfigKey(configKey);

        if (config.isPresent()) {
            EmailQuotaConfig quotaConfig = config.get();
            quotaConfig.setConfigValue("true");
            emailQuotaConfigRepository.save(quotaConfig);
        } else {
            // 如果配置不存在，建立新配置
            EmailQuotaConfig newConfig = new EmailQuotaConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue("true");
            newConfig.setDescription("告警已發送標記");
            emailQuotaConfigRepository.save(newConfig);
        }

        log.info("告警標記已更新：{} = true", configKey);
    }

    /**
     * 重置所有告警標記（每月初自動執行）
     * 在新的月份開始時重置告警狀態
     */
    @Scheduled(cron = "0 0 0 1 * ?")  // 每月1號凌晨執行
    public void resetAlertFlags() {
        try {
            List<EmailQuotaConfig> alertConfigs = emailQuotaConfigRepository.findAll().stream()
                    .filter(config -> config.getConfigKey().startsWith("alert_threshold_"))
                    .toList();

            for (EmailQuotaConfig config : alertConfigs) {
                config.setConfigValue("false");
                emailQuotaConfigRepository.save(config);
            }

            log.info("已重置所有郵件配額告警標記（共 {} 個）", alertConfigs.size());
        } catch (Exception e) {
            log.error("重置告警標記失敗", e);
        }
    }

    /**
     * 手動觸發配額檢查（用於測試或手動檢查）
     *
     * @return 當前使用率
     */
    public double manualCheckQuota() {
        double usagePercentage = emailQuotaService.getCurrentMonthUsagePercentage();
        long currentUsage = emailQuotaService.getCurrentMonthUsage();

        log.info("手動檢查郵件配額：使用率 {}%，已發送 {} 封",
                String.format("%.2f", usagePercentage), currentUsage);

        return usagePercentage;
    }
}
