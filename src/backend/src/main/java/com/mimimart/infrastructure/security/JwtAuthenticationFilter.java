package com.mimimart.infrastructure.security;

import com.mimimart.shared.valueobject.UserType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 認證過濾器
 * 負責從請求中提取 JWT Token 並驗證
 * 支援前台會員與後台管理員的雙用戶類型
 *
 * @author MimiMart Development Team
 * @since 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 從 Header 提取 JWT Token
            String token = extractTokenFromRequest(request);

            if (token != null && jwtUtil.validateToken(token)) {
                // 從 Token 提取 Email 和 UserType
                String email = jwtUtil.extractEmail(token);
                UserType userType = jwtUtil.extractUserType(token);

                // 根據請求路徑驗證 userType 匹配
                String requestPath = request.getRequestURI();
                validateUserTypeForPath(requestPath, userType);

                // 載入用戶詳情
                UserDetails userDetails = customUserDetailsService.loadUserByUsernameAndType(email, userType);

                // 建立 Authentication 物件
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 設定到 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 記錄異常但不中斷請求 (讓 Spring Security 處理未認證的請求)
            logger.error("JWT 認證失敗: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 驗證 UserType 與請求路徑是否匹配
     *
     * @param requestPath 請求路徑
     * @param userType 用戶類型
     * @throws SecurityException 如果類型不匹配
     */
    private void validateUserTypeForPath(String requestPath, UserType userType) {
        // 後台路徑要求 ADMIN 類型
        if (requestPath.startsWith("/api/admin/") && userType != UserType.ADMIN) {
            throw new SecurityException("後台端點要求管理員權限");
        }

        // 前台會員專用路徑要求 MEMBER 類型
        if ((requestPath.startsWith("/api/storefront/member/") ||
             requestPath.startsWith("/api/storefront/address/")) &&
            userType != UserType.MEMBER) {
            throw new SecurityException("會員端點要求會員權限");
        }
    }

    /**
     * 從請求 Header 提取 Bearer Token
     *
     * @param request HttpServletRequest
     * @return JWT Token (不含 "Bearer " 前綴)
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
