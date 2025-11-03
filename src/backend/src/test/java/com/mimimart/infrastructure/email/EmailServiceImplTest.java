package com.mimimart.infrastructure.email;

import com.mimimart.application.service.EmailQuotaService;
import com.mimimart.application.service.EmailRateLimitService;
import com.mimimart.application.service.EmailSendLogService;
import com.mimimart.application.service.EmailService;
import com.mimimart.infrastructure.email.sender.EmailSender;
import com.mimimart.shared.valueobject.EmailType;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * EmailService 單元測試
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService 單元測試")
class EmailServiceImplTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private EmailQuotaService emailQuotaService;

    @Mock
    private EmailRateLimitService emailRateLimitService;

    @Mock
    private EmailSendLogService emailSendLogService;

    @InjectMocks
    private EmailServiceImpl emailService;

    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "測試會員";
    private static final String TEST_TOKEN = "test-token-123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", FRONTEND_URL);
    }

    @Test
    @DisplayName("發送歡迎郵件 - 成功")
    void sendWelcomeEmail_Success() throws Exception {
        // Given
        String htmlContent = "<html>Welcome</html>";
        when(templateEngine.process(eq("email/welcome"), any(Context.class)))
                .thenReturn(htmlContent);

        // When
        emailService.sendWelcomeEmail(TEST_EMAIL, TEST_NAME);

        // Then
        verify(templateEngine).process(eq("email/welcome"), any(Context.class));
        verify(emailSender).send(eq(TEST_EMAIL), eq("歡迎加入 MimiMart！"), eq(htmlContent));
        verify(emailQuotaService).checkAndIncrementQuota();
        verify(emailSendLogService).logEmailSent(isNull(), eq(TEST_EMAIL), eq(EmailType.WELCOME), eq("歡迎加入 MimiMart！"));
    }


    @Test
    @DisplayName("發送驗證郵件 - 成功")
    void sendVerificationEmail_Success() throws Exception {
        // Given
        String htmlContent = "<html>Verify your email</html>";
        when(templateEngine.process(eq("email/verification"), any(Context.class)))
                .thenReturn(htmlContent);

        // When
        emailService.sendVerificationEmail(TEST_EMAIL, TEST_NAME, TEST_TOKEN);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/verification"), contextCaptor.capture());
        verify(emailSender).send(eq(TEST_EMAIL), eq("請驗證您的 Email"), eq(htmlContent));
        verify(emailQuotaService).checkAndIncrementQuota();
        verify(emailSendLogService).logEmailSent(isNull(), eq(TEST_EMAIL), eq(EmailType.VERIFICATION), eq("請驗證您的 Email"));

        Context context = contextCaptor.getValue();
        assertThat(context.getVariable("memberName")).isEqualTo(TEST_NAME);
        assertThat(context.getVariable("token")).isEqualTo(TEST_TOKEN);
        assertThat(context.getVariable("verificationUrl")).asString()
                .contains(FRONTEND_URL)
                .contains(TEST_TOKEN);
    }


    @Test
    @DisplayName("發送密碼重設郵件 - 成功")
    void sendPasswordResetEmail_Success() throws Exception {
        // Given
        String htmlContent = "<html>Reset your password</html>";
        when(templateEngine.process(eq("email/password-reset"), any(Context.class)))
                .thenReturn(htmlContent);

        // When
        emailService.sendPasswordResetEmail(TEST_EMAIL, TEST_NAME, TEST_TOKEN);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/password-reset"), contextCaptor.capture());
        verify(emailSender).send(eq(TEST_EMAIL), eq("重設您的密碼"), eq(htmlContent));
        verify(emailQuotaService).checkAndIncrementQuota();
        verify(emailSendLogService).logEmailSent(isNull(), eq(TEST_EMAIL), eq(EmailType.PASSWORD_RESET), eq("重設您的密碼"));

        Context context = contextCaptor.getValue();
        assertThat(context.getVariable("memberName")).isEqualTo(TEST_NAME);
        assertThat(context.getVariable("token")).isEqualTo(TEST_TOKEN);
        assertThat(context.getVariable("resetUrl")).asString()
                .contains(FRONTEND_URL)
                .contains(TEST_TOKEN);
    }

}
