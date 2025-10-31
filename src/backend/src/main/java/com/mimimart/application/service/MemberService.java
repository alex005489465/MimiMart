package com.mimimart.application.service;

import com.mimimart.domain.member.exception.*;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.infrastructure.storage.S3StorageService;
import com.mimimart.shared.validation.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 會員服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3StorageService s3StorageService;

    public MemberService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            S3StorageService s3StorageService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3StorageService = s3StorageService;
    }

    /**
     * 查詢會員資料
     */
    public Member getProfile(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));
    }

    /**
     * 更新會員資料
     */
    @Transactional
    public Member updateProfile(Long memberId, String name, String phone, String homeAddress) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));

        if (name != null) {
            member.setName(name);
        }
        if (phone != null) {
            member.setPhone(phone);
        }
        if (homeAddress != null) {
            member.setHomeAddress(homeAddress);
        }

        return memberRepository.save(member);
    }

    /**
     * 修改密碼
     */
    @Transactional
    public void changePassword(Long memberId, String oldPassword, String newPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));

        // 驗證舊密碼
        if (!passwordEncoder.matches(oldPassword, member.getPasswordHash())) {
            throw new InvalidCredentialsException("舊密碼錯誤");
        }

        // 更新密碼
        member.setPasswordHash(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    /**
     * 上傳會員頭貼
     *
     * @param memberId 會員 ID
     * @param file     上傳的檔案
     * @return 更新後的會員資料
     */
    @Transactional
    public Member uploadAvatar(Long memberId, MultipartFile file) {
        // 驗證檔案
        try {
            FileValidator.validateImageFile(file);
        } catch (IllegalArgumentException e) {
            throw new InvalidAvatarException(e.getMessage());
        }

        // 查詢會員
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));

        // 刪除舊頭貼(如果存在)
        if (member.getAvatarS3Key() != null && !member.getAvatarS3Key().isEmpty()) {
            try {
                s3StorageService.deleteAvatar(member.getAvatarS3Key());
                log.info("已刪除舊頭貼 - MemberId: {}, S3 Key: {}", memberId, member.getAvatarS3Key());
            } catch (Exception e) {
                log.warn("刪除舊頭貼失敗,繼續上傳新頭貼 - MemberId: {}, Error: {}", memberId, e.getMessage());
            }
        }

        // 上傳新頭貼到 S3
        String s3Key = s3StorageService.uploadAvatar(memberId, file);

        // 更新資料庫記錄
        member.setAvatarS3Key(s3Key);
        member.setAvatarUrl(s3Key); // 暫時存 S3 key 作為識別
        member.setAvatarUpdatedAt(LocalDateTime.now());

        return memberRepository.save(member);
    }

    /**
     * 取得會員頭貼資料
     *
     * @param memberId          頭貼所屬會員 ID
     * @param requesterMemberId 請求者會員 ID
     * @return 頭貼的位元組陣列
     */
    public byte[] getAvatarData(Long memberId, Long requesterMemberId) {
        // 權限驗證:只能讀取自己的頭貼
        if (!memberId.equals(requesterMemberId)) {
            throw new UnauthorizedAvatarAccessException("無權訪問該會員的頭貼");
        }

        // 查詢會員
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));

        // 檢查是否有頭貼
        if (member.getAvatarS3Key() == null || member.getAvatarS3Key().isEmpty()) {
            throw new AvatarNotFoundException("該會員尚未上傳頭貼");
        }

        // 從 S3 下載頭貼
        try {
            return s3StorageService.downloadAvatar(member.getAvatarS3Key());
        } catch (Exception e) {
            log.error("下載頭貼失敗 - MemberId: {}, S3 Key: {}, Error: {}",
                    memberId, member.getAvatarS3Key(), e.getMessage(), e);
            throw new AvatarNotFoundException("頭貼下載失敗");
        }
    }

    /**
     * 取得會員頭貼的 Content-Type
     *
     * @param memberId 會員 ID
     * @return Content-Type
     */
    public String getAvatarContentType(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在"));

        if (member.getAvatarS3Key() == null || member.getAvatarS3Key().isEmpty()) {
            throw new AvatarNotFoundException("該會員尚未上傳頭貼");
        }

        return s3StorageService.getContentType(member.getAvatarS3Key());
    }
}
