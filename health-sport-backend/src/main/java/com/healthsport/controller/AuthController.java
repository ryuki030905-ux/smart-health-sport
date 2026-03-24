package com.healthsport.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.healthsport.dto.LoginDTO;
import com.healthsport.dto.RegisterDTO;
import com.healthsport.entity.User;
import com.healthsport.exception.BusinessException;
import com.healthsport.mapper.UserMapper;
import com.healthsport.security.JwtUtils;
import com.healthsport.utils.Result;
import com.healthsport.vo.LoginVO;
import com.healthsport.vo.UserVO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 认证控制器：注册 / 登录 / 登出
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    /** Redis 黑名单 key 前缀，与 JwtAuthFilter 保持一致 */
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    public AuthController(UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils,
                          StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (existing != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .gender(dto.getGender() == null ? 0 : dto.getGender())
                .age(dto.getAge())
                .occupation(dto.getOccupation())
                .role("USER")
                .status(1)
                .build();
        userMapper.insert(user);

        UserVO vo = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
        return Result.success(vo);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getId());
        Claims claims = jwtUtils.parseToken(token);
        claims.put("role", user.getRole());

        LoginVO vo = LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpireSeconds())
                .userInfo(UserVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .role(user.getRole())
                        .build())
                .build();
        return Result.success(vo);
    }

    /**
     * 登出接口：将当前 token 加入 Redis 黑名单，
     * 有效期与 token 剩余有效期一致，确保在 token 真正过期前无法再使用。
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return Result.success(null); // 没带 token 也按成功处理
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.success(null); // token 已经无效，同样按成功处理
        }

        // 计算 token 剩余有效期，设置为黑名单过期时间
        long remainingSeconds = jwtUtils.getRemainingExpiration(token);
        if (remainingSeconds > 0) {
            stringRedisTemplate.opsForValue()
                    .set(BLACKLIST_PREFIX + token, "1", remainingSeconds, TimeUnit.SECONDS);
        }

        return Result.success(null);
    }
}
