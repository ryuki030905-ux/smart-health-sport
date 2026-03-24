package com.healthsport.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
// JWT 工具类。
// 登录成功后会生成 token，后面前端请求接口时再把它带回来。
// token 的生成、解析、校验都放在这里处理。
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-seconds}")
    private Long expireSeconds;

    // 这里负责生成 token。
    // token 里会存用户名和 userId，后面一解析就能知道当前是谁在访问。
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 token，拿到里面保存的 claims。
    // claims 可以理解成 token 里顺手带的一些信息，比如 userId 这些。
    public Claims parseToken(String token) {
        Jws<Claims> jwsClaims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);
        return jwsClaims.getBody();
    }

    // 看 token 还能不能正常用。
    // 只要过期了、被改过或者格式不对，这里就会返回 false。
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // 返回 token 的过期时间，前端处理登录状态时会用到。
    public Long getExpireSeconds() {
        return expireSeconds;
    }

    // 生成签名要用的 key。
    // HS256 对 key 长度有要求，所以如果 secret 太短，这里会先补到 32 字节。
    /**
     * 计算 token 剩余有效期（秒），用于设置 Redis 黑名单的过期时间。
     */
    public long getRemainingExpiration(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return 0;
            }
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingMs / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    private Key getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

