package com.mimimart.application.service;

import com.mimimart.domain.member.exception.*;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.infrastructure.persistence.specification.MemberSpecification;
import com.mimimart.infrastructure.storage.S3StorageService;
import com.mimimart.shared.validation.FileValidator;
import com.mimimart.shared.valueobject.MemberStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
     * 取得會員頭貼資料 (公開訪問)
     *
     * @param memberId 頭貼所屬會員 ID
     * @return 頭貼的位元組陣列
     */
    public byte[] getAvatarData(Long memberId) {
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

    // ==================== 管理員專用方法 ====================

    /**
     * 查詢會員列表（管理員專用）
     * 支援搜尋、篩選、排序、分頁
     *
     * @param keyword    搜尋關鍵字（支援 Email、姓名、電話、會員 ID）
     * @param status     帳號狀態篩選
     * @param startDate  註冊開始日期
     * @param endDate    註冊結束日期
     * @param pageable   分頁與排序參數
     * @return 會員分頁資料
     */
    public Page<Member> getMemberListForAdmin(
            String keyword,
            MemberStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        // 建立查詢條件
        Specification<Member> specification = MemberSpecification.buildSpecification(
                keyword, status, startDate, endDate
        );

        // 執行查詢
        return memberRepository.findAll(specification, pageable);
    }

    /**
     * 查詢會員詳細資料（管理員專用）
     *
     * @param memberId 會員 ID
     * @return 會員資料
     */
    public Member getMemberDetailForAdmin(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("會員不存在：ID=" + memberId));
    }
}
