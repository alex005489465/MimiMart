package com.mimimart.application.service;

import com.mimimart.domain.member.exception.InvalidCredentialsException;
import com.mimimart.domain.member.exception.MemberNotFoundException;
import com.mimimart.infrastructure.persistence.entity.Member;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 會員服務
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
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
}
