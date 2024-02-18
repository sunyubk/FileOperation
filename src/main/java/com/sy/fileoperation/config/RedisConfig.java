package com.sy.fileoperation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @ClassName RedisConfig
 * @Description
 * @Author sunyu
 * @Date 2024/2/17 14:09
 * @Version 1.0
 **/
@Configuration
public class RedisConfig {
    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
        configureRedisTemplate();
    }

    private void configureRedisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用 JSON 序列化
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.afterPropertiesSet();
    }
}
