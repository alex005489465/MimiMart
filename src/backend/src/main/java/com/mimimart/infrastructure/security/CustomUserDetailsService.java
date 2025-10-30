package com.mimimart.infrastructure.security;

import com.mimimart.infrastructure.persistence.repository.AdminRepository;
import com.mimimart.infrastructure.persistence.repository.MemberRepository;
import com.mimimart.shared.valueobject.UserType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自訂 UserDetailsService
 * 支援會員與管理員的雙用戶類型載入
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(MemberRepository memberRepository,
                                   AdminRepository adminRepository) {
        this.memberRepository = memberRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 預設載入會員
        return loadUserByUsernameAndType(email, UserType.MEMBER);
    }

    /**
     * 根據 Email 和用戶類型載入用戶
     *
     * @param email Email
     * @param userType 用戶類型
     * @return UserDetails
     * @throws UsernameNotFoundException 用戶不存在
     */
    public UserDetails loadUserByUsernameAndType(String email, UserType userType) throws UsernameNotFoundException {
        if (userType == UserType.ADMIN) {
            return adminRepository.findByEmail(email)
                    .map(admin -> new CustomUserDetails(
                            admin.getId(),
                            admin.getEmail(),
                            admin.getPasswordHash(),
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")),
                            UserType.ADMIN
                    ))
                    .orElseThrow(() -> new UsernameNotFoundException("管理員不存在: " + email));
        } else {
            return memberRepository.findByEmail(email)
                    .map(member -> new CustomUserDetails(
                            member.getId(),
                            member.getEmail(),
                            member.getPasswordHash(),
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER")),
                            UserType.MEMBER
                    ))
                    .orElseThrow(() -> new UsernameNotFoundException("會員不存在: " + email));
        }
    }
}
