package com.demo.agent.common.redis;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RedisOperation {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 续期Redis键的过期时间
     */
    public boolean renewal(String key, Duration expireTime) {
        Boolean expire = redisTemplate.expire(key, expireTime);
        return Boolean.TRUE.equals(expire);
    }

    /**
     * 删除Redis键
     */
    public boolean remove(String key) {
        Boolean delete = redisTemplate.delete(key);
        return Boolean.TRUE.equals(delete);
    }

    /**
     * 写入数据到Redis
     */
    public void write(String key, String value, Duration expireTime) {
        redisTemplate.opsForValue().set(key, value, expireTime);
    }

    /**
     * 从Redis读取数据
     */
    public String read(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取键的剩余过期时间（秒）
     */
    public Long getTtl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 检查键是否存在
     */
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
