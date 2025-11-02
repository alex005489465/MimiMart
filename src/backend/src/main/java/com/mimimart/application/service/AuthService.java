package com.mimimart.application.service;

import com.mimimart.domain.member.exception.AccountDisabledException;
import com.mimimart.domain.member.exception.EmailAlreadyVerifiedException;
import com.mimimart.domain.member.exception.InvalidCredentialsException;
import com.mimimart.domain.member.exception.InvalidResetTokenException;
import com.mimimart.domain.member.exception.InvalidVerificationTokenException;
import com.mimimart.domain.member.exception.MemberAlreadyExistsException;
import com.mimimart.domain.member.exception.MemberNotFoundException;
import com.mimimart.domain.member.exception.PasswordMismatchException;
import com.mimimart.domain.member.exception.ResetTokenExpiredException;
import com.mimimart.domain.member.exception.VerificationTokenExpiredException;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.infrastructure.security.JwtUtil;
import com.mimimart.shared.valueobject.MemberStatus;
import com.mimimart.shared.valueobject.UserType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 會員認證服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    public AuthService(MemberRepository memberRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      RefreshTokenService refreshTokenService,
                      EmailService emailService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
    }

    /**
     * 會員註冊
     */
    @Transactional
    public Member register(String email, String password, String name) {
        // 檢查 Email 是否已存在
        if (memberRepository.existsByEmail(email)) {
            throw new MemberAlreadyExistsException("此 Email 已被註冊");
        }

        // 建立會員
        Member member = new Member();
        member.setEmail(email);
        member.setPasswordHash(passwordEncoder.encode(password));
        member.setName(name);
        member.setStatus(MemberStatus.ACTIVE);
        member.setEmailVerified(false);

        // 生成 Email 驗證 Token
        String verificationToken = UUID.randomUUID().toString();
        member.setVerificationToken(verificationToken);
        member.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));

        Member savedMember = memberRepository.save(member);

        // 發送歡迎郵件和驗證郵件
        emailService.sendWelcomeEmail(email, name);
        emailService.sendVerificationEmail(email, name, verificationToken);

        return savedMember;
    }

    /**
     * 會員登入
     */
    @Transactional
    public LoginResult login(String email, String password) {
        // 查詢會員
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Email 或密碼錯誤"));

        // 驗證密碼
        if (!passwordEncoder.matches(password, member.getPasswordHash())) {
            throw new InvalidCredentialsException("Email 或密碼錯誤");
        }

        // 檢查帳號狀態
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AccountDisabledException("帳號已被停用或封禁");
        }

        // 更新最後登入時間
        member.setLastLoginAt(LocalDateTime.now());
        memberRepository.save(member);

        // 生成 Access Token 和 Refresh Token
        String accessToken = jwtUtil.generateAccessToken(member.getId(), member.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        // 儲存 Refresh Token
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusDays(7);
        refreshTokenService.saveRefreshToken(member.getId(), refreshToken, UserType.MEMBER, refreshTokenExpiresAt);

        return new LoginResult(accessToken, refreshToken, member);
    }

    /**
     * 會員登出
     */
    @Transactional
    public void logout(Long memberId) {
        refreshTokenService.revokeAllTokens(memberId, UserType.MEMBER);
    }

    /**
     * 更新 Access Token
     */
    @Transactional
    public TokenRefreshResult refreshAccessToken(String refreshToken) {
        // 驗證 Refresh Token
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException("Refresh Token 無效或已過期");
        }

        Long memberId = refreshTokenService.getUserIdByToken(refreshToken);
        if (memberId == null) {
            throw new InvalidCredentialsException("Refresh Token 無效");
        }

        // 查詢會員
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));

        // 檢查帳號狀態
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AccountDisabledException("帳號已被停用或封禁");
        }

        // 生成新的 Access Token
        String newAccessToken = jwtUtil.generateAccessToken(member.getId(), member.getEmail());

        return new TokenRefreshResult(newAccessToken);
    }

    /**
     * 登入結果
     */
    public static class LoginResult {
        public final String accessToken;
        public final String refreshToken;
        public final Member member;

        public LoginResult(String accessToken, String refreshToken, Member member) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.member = member;
        }
    }

    /**
     * Token 更新結果
     */
    public static class TokenRefreshResult {
        public final String accessToken;

        public TokenRefreshResult(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    /**
     * 驗證 Email
     */
    @Transactional
    public void verifyEmail(String token) {
        // 查詢會員
        Member member = memberRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("無效的驗證 Token"));

        // 檢查是否已驗證
        if (member.getEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email 已經驗證過了");
        }

        // 檢查 Token 是否過期
        if (member.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationTokenExpiredException("驗證 Token 已過期，請重新申請");
        }

        // 更新驗證狀態
        member.setEmailVerified(true);
        member.setVerificationToken(null);
        member.setVerificationTokenExpiresAt(null);
        memberRepository.save(member);
    }

    /**
     * 重新發送驗證郵件
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        // 查詢會員
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));

        // 檢查是否已驗證
        if (member.getEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email 已經驗證過了");
        }

        // 生成新的驗證 Token
        String verificationToken = UUID.randomUUID().toString();
        member.setVerificationToken(verificationToken);
        member.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));
        memberRepository.save(member);

        // 發送驗證郵件
        emailService.sendVerificationEmail(email, member.getName(), verificationToken);
    }

    /**
     * 申請密碼重設
     */
    @Transactional
    public void requestPasswordReset(String email) {
        // 查詢會員
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));

        // 生成密碼重設 Token（30 分鐘有效期）
        String resetToken = UUID.randomUUID().toString();
        member.setPasswordResetToken(resetToken);
        member.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
        memberRepository.save(member);

        // 發送密碼重設郵件
        emailService.sendPasswordResetEmail(email, member.getName(), resetToken);
    }

    /**
     * 重設密碼
     */
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        // 檢查密碼是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("新密碼與確認密碼不一致");
        }

        // 查詢會員
        Member member = memberRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidResetTokenException("無效的密碼重設 Token"));

        // 檢查 Token 是否過期
        if (member.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResetTokenExpiredException("密碼重設 Token 已過期，請重新申請");
        }

        // 更新密碼
        member.setPasswordHash(passwordEncoder.encode(newPassword));
        member.setPasswordResetToken(null);
        member.setPasswordResetTokenExpiresAt(null);
        memberRepository.save(member);

        // 撤銷所有 Refresh Token（強制重新登入）
        refreshTokenService.revokeAllTokens(member.getId(), UserType.MEMBER);
    }
}
