package com.healthsport.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthsport.entity.User;
import com.healthsport.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 每次请求进来都从 Authorization Header 里解析 token，
 * 并从 Redis 黑名单中检查该 token 是否已被主动注销。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Redis 黑名单 key 前缀：blacklist:{token} */
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtAuthFilter(JwtUtils jwtUtils,
                         UserMapper userMapper,
                         StringRedisTemplate stringRedisTemplate) {
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // ① token 本身格式合法
            // ② 当前请求还没有认证信息（避免重复注入）
            // ③ token 不在 Redis 黑名单里（未被 logout 拉黑）
            if (jwtUtils.validateToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && !isTokenBlacklisted(token)) {

                Claims claims = jwtUtils.parseToken(token);
                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);
                String roleCode = StringUtils.hasText(role) ? role : loadRoleByUserId(userId);

                JwtUserPrincipal principal = new JwtUserPrincipal(userId, username, roleCode);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleCode))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查 token 是否在 Redis 黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        Boolean exists = stringRedisTemplate.hasKey(BLACKLIST_PREFIX + token);
        return Boolean.TRUE.equals(exists);
    }

    private String loadRoleByUserId(Long userId) {
        if (userId == null) {
            return "USER";
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userId));
        if (user == null || !StringUtils.hasText(user.getRole())) {
            return "USER";
        }
        return user.getRole();
    }
}
