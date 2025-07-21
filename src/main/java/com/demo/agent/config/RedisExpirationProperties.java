package com.demo.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis过期监控配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "redis.expiration")
public class RedisExpirationProperties {

    /**
     * 过期阈值（秒），当键的剩余过期时间小于此值时，触发持久化
     */
    private long thresholdSeconds = 60;

    /**
     * 定时任务执行间隔（毫秒）
     */
    private long scanIntervalMs = 30000;

    /**
     * 是否启用过期监控
     */
    private boolean enabled = true;
}