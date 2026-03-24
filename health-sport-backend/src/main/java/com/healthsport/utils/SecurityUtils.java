package com.healthsport.utils;

import com.healthsport.exception.BusinessException;
import com.healthsport.security.JwtUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// 和当前登录用户有关的小工具放这里
public class SecurityUtils {

    private SecurityUtils() {
    }

    // 拿当前登录用户的 id
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(401, "用户未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.getUserId();
        }
        throw new BusinessException(401, "用户身份无效");
    }
}

