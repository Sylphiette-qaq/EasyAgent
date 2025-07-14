package com.demo.agent.service;

import com.demo.agent.entity.AiChatResponse;
import cn.hutool.json.JSONUtil;
import com.demo.agent.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天上下文服务，基于Redis存储
 */
@Service
public class ChatContextService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CONTEXT_PREFIX = "chat:context:";
    private static final Duration EXPIRE = Duration.ofMinutes(30);

    /**
     * 保存上下文
     */
    public void saveContext(String sessionId, List<Message> messages) {
        String key = CONTEXT_PREFIX + sessionId;
        String value = JSONUtil.toJsonStr(messages);
        redisTemplate.opsForValue().set(key, value, EXPIRE);
    }

    /**
     * 获取上下文
     */
    public List<Message> getContext(String sessionId) {
        String key = CONTEXT_PREFIX + sessionId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return new ArrayList<>();
        return JSONUtil.toList(JSONUtil.parseArray(value), Message.class);
    }

    /**
     * 清理上下文
     */
    public void clearContext(String sessionId) {
        String key = CONTEXT_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
} 