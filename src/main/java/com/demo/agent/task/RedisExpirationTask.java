package com.demo.agent.task;

import com.demo.agent.common.rabbitmq.MyMessageProducer;
import com.demo.agent.common.redis.RedisOperation;
import com.demo.agent.config.RedisExpirationProperties;
import com.demo.agent.model.base.RabbitMqTransportEntity;
import com.demo.agent.model.entity.SessionEntity;
import com.demo.agent.service.session.SessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.demo.agent.common.Constants.*;

/**
 * Redis键过期监控定时任务
 * 简化版本：只使用定时任务扫描，不使用乐观锁
 */
@Slf4j
@Component
public class RedisExpirationTask {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisOperation redisOperation;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RedisExpirationProperties expirationProperties;

    @Resource
    private MyMessageProducer myMessageProducer;

    /**
     * 定时扫描Redis中即将过期的键
     * 执行间隔由配置文件决定
     */
    @Scheduled(fixedRateString = "#{@redisExpirationProperties.scanIntervalMs}")
    public void scanExpiringKeys() {
        // 检查是否启用过期监控
        if (!expirationProperties.isEnabled()) {
            return;
        }

        try {
            log.debug("开始扫描即将过期的Redis键");

            // 获取所有消息内存相关的键
            Set<String> keys = redisTemplate.keys(MESSAGE_MEMORY_PREFIX + "*");

            if (keys == null || keys.isEmpty()) {
                log.debug("未找到任何消息内存键");
                return;
            }
            for (String key : keys) {
                    // 获取键的剩余过期时间
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

                    // 如果键不存在或已过期，跳过
                    if (ttl == null || ttl <= 0) {
                        continue;
                    }

                    // 如果剩余时间小于阈值，触发持久化
                    if (ttl <= expirationProperties.getThresholdSeconds()) {
                        persistKeyToDatabase(key);
                    }
            }
        } catch (Exception e) {
            log.error("扫描即将过期的Redis键时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 将即将过期的键持久化到数据库
     *
     * @param key Redis键
     * @return 是否成功持久化
     */
    private void persistKeyToDatabase(String key) {
        try {
            // 从键中提取sessionId
            String sessionIdStr = key.replace(MESSAGE_MEMORY_PREFIX, "");
            Long sessionId = Long.parseLong(sessionIdStr);
            log.debug("即将持久化的键 {} ", key);
            // 从Redis获取内容
            String content = redisOperation.read(key);
            if (content == null || content.trim().isEmpty()) {
                log.warn("键 {} 的内容为空，跳过持久化", key);
            }
            RabbitMqTransportEntity rabbitMqTransportEntity = new RabbitMqTransportEntity();
            rabbitMqTransportEntity.setId(sessionId);
            rabbitMqTransportEntity.setContent(content);
            rabbitMqTransportEntity.setDateTime(new Date());
            ObjectMapper objectMapper = new ObjectMapper();
            String messageJson = "";
            try {
                messageJson = objectMapper.writeValueAsString(rabbitMqTransportEntity);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            myMessageProducer.sendMessage(EXCHANGE_NAME, ROUTING_KEY, messageJson);


        } catch (NumberFormatException e) {
            log.error("无法解析会话ID，键: {}", key);
        } catch (Exception e) {
            log.error("持久化键 {} 到数据库时发生错误: {}", key, e.getMessage(), e);
        }
    }
}