package com.healthsport.utils;

import com.healthsport.entity.User;
import com.healthsport.exception.BusinessException;
import com.healthsport.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的滑动窗口限流器。
 * 用于 AI 接口的每日调用次数限制（每个用户每天最多调用 N 次）。
 *
 * 使用方式：在 Controller 或 Service 层调用 check()，
 * 超过限制时抛出 BusinessException。
 */
@Component
public class RateLimiter {

    private static final String KEY_PREFIX = "ai:ratelimit:";

    private final StringRedisTemplate stringRedisTemplate;
    private final int defaultDailyLimit;
    private final UserMapper userMapper;

    public RateLimiter(StringRedisTemplate stringRedisTemplate,
                       UserMapper userMapper,
                       @Value("${spring.ai.cache.daily-limit:3}") int defaultDailyLimit) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userMapper = userMapper;
        this.defaultDailyLimit = defaultDailyLimit;
    }

    /**
     * 检查当前用户是否超过每日调用次数上限。
     *
     * @param userId 用户 ID（从 SecurityUtils 获取）
     * @throws BusinessException 超过限制时抛出
     */
    public void check(Long userId) {
        if (userId == null) {
            return; // 没登录不限制
        }

        User user = getUser(userId);
        if (isAdmin(user)) {
            return;
        }

        int dailyLimit = getDailyLimit(user);
        String key = buildKey(userId);
        Long current = stringRedisTemplate.opsForValue().increment(key);

        if (current != null && current == 1L) {
            stringRedisTemplate.expire(key, getSecondsUntilMidnight(), TimeUnit.SECONDS);
        }

        if (current != null && current > dailyLimit) {
            throw new BusinessException(429,
                    "今日 AI 建议次数已用完（每日上限 " + dailyLimit + " 次），明天再试吧！");
        }
    }

    /**
     * 查询当前用户今日已调用次数
     */
    public int getTodayCount(Long userId) {
        if (userId == null) {
            return 0;
        }

        String value = stringRedisTemplate.opsForValue().get(buildKey(userId));
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * 查询当前用户今日剩余可用次数
     */
    public int getRemaining(Long userId) {
        if (userId == null) {
            return defaultDailyLimit;
        }

        User user = getUser(userId);
        if (isAdmin(user)) {
            return Integer.MAX_VALUE;
        }

        return Math.max(0, getDailyLimit(user) - getTodayCount(userId));
    }

    private User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private int getDailyLimit(User user) {
        if (user == null || user.getAiDailyLimit() == null) {
            return defaultDailyLimit;
        }
        return Math.max(0, user.getAiDailyLimit());
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId + ":" + java.time.LocalDate.now();
    }

    /**
     * 计算距离当天午夜剩余的秒数
     */
    private long getSecondsUntilMidnight() {
        long now = System.currentTimeMillis();
        long midnight = java.time.LocalDate.now()
                .plusDays(1)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        return (midnight - now) / 1000;
    }
}
