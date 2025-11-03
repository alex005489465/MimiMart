package com.mimimart.infrastructure.email.sender;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * AWS SES SDK 郵件發送實作
 *
 * <p>使用 AWS SDK v2 直接呼叫 SES API 發送郵件。
 * 適用於生產環境，支援高併發與大量發送。
 *
 * <p><b>認證方式</b>：直接使用 AWS Access Key + Secret Key，
 * 無需轉換為 SMTP Password。
 *
 * @author MimiMart Team
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mimimart.email.provider", havingValue = "aws-ses")
public class AwsSesEmailSender implements EmailSender {

    @Value("${mimimart.email.aws.ses.region:ap-south-1}")
    private String awsRegion;

    @Value("${AWS_ACCESS_KEY_ID}")
    private String accessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String secretAccessKey;

    @Value("${mimimart.email.from.address}")
    private String fromAddress;

    @Value("${mimimart.email.from.name:MimiMart}")
    private String fromName;

    private SesClient sesClient;

    @PostConstruct
    public void init() {
        log.info("初始化 AWS SES Client: region={}", awsRegion);

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        this.sesClient = SesClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("AWS SES Client 初始化完成");
    }

    @PreDestroy
    public void destroy() {
        if (sesClient != null) {
            sesClient.close();
            log.info("AWS SES Client 已關閉");
        }
    }

    @Override
    public void send(String to, String subject, String htmlContent) throws MessagingException {
        log.debug("使用 AWS SES 發送郵件至: {}", to);

        try {
            // 構建郵件內容
            Content subjectContent = Content.builder()
                    .charset("UTF-8")
                    .data(subject)
                    .build();

            Content htmlBodyContent = Content.builder()
                    .charset("UTF-8")
                    .data(htmlContent)
                    .build();

            Body body = Body.builder()
                    .html(htmlBodyContent)
                    .build();

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(body)
                    .build();

            // 構建發送請求
            Destination destination = Destination.builder()
                    .toAddresses(to)
                    .build();

            // 設定寄件者（支援顯示名稱）
            String fromAddressWithName = String.format("%s <%s>", fromName, fromAddress);

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(fromAddressWithName)
                    .destination(destination)
                    .message(message)
                    .build();

            // 發送郵件
            SendEmailResponse response = sesClient.sendEmail(emailRequest);

            log.info("AWS SES 郵件發送成功: to={}, subject={}, messageId={}",
                    to, subject, response.messageId());

        } catch (SesException e) {
            log.error("AWS SES 郵件發送失敗: to={}, errorCode={}, errorMessage={}",
                    to, e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage());
            throw new MessagingException("AWS SES 郵件發送失敗: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("AWS SES 郵件發送發生未預期錯誤: to={}", to, e);
            throw new MessagingException("AWS SES 郵件發送失敗", e);
        }
    }
}
