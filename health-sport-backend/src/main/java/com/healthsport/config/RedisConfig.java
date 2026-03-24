package com.healthsport.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 全局配置：序列化器 + Spring Cache 管理器
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * RedisTemplate：key 用 String，value 用 JSON（支持 Java 对象）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key 序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // value 序列化：Jackson
        Jackson2JsonRedisSerializer<Object> jsonSerializer = jackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * StringRedisTemplate：专门用来操作 Redis 的 String（key=value）结构
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    /**
     * Spring Cache 缓存管理器，统一配置缓存过期时间和序列化方式
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> jsonSerializer = jackson2JsonRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))                     // 默认 1 小时过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                // 单独配置各缓存命名空间的有效期
                .withCacheConfiguration("exerciseDict",
                        defaultConfig.entryTtl(Duration.ofHours(12)))
                .withCacheConfiguration("foodDict",
                        defaultConfig.entryTtl(Duration.ofHours(12)))
                .withCacheConfiguration("calorieBalance",
                        defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("weightTrend",
                        defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("weeklyExercise",
                        defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("aiAdvice",
                        defaultConfig.entryTtl(Duration.ofHours(24)))
                .build();
    }

    /**
     * 构建 Jackson JSON 序列化器，支持 Java 8 时间类型
     */
    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonRedisSerializer<>(mapper, Object.class);
    }
}
