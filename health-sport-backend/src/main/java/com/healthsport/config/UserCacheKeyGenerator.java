package com.healthsport.config;

import com.healthsport.utils.SecurityUtils;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 自定义缓存 key 生成器。
 * 所有需要按用户隔离的缓存都使用此生成器，
 * 保证不同用户的同名缓存不会相互覆盖。
 *
 * 生成规则：方法名(参数1, 参数2, ...)::userId=xxx
 */
@Component("userKeyGenerator")
public class UserCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append("(");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(params[i] == null ? "null" : params[i].toString());
        }
        sb.append(")");
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            sb.append("::userId=").append(userId);
        }
        return sb.toString();
    }
}
