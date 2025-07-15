package com.demo.agent.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.demo.agent.entity.User;
import com.demo.agent.mapper.UserMapper;
import com.demo.agent.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "auth:token:";
    private static final Duration TOKEN_EXPIRE = Duration.ofHours(2);

    @Override
    public String login(String username, String password) {
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("username", username));
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        String key = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, user.getId().toString(), TOKEN_EXPIRE);
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        String key = TOKEN_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    @Override
    public void logout(String token) {
        String key = TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }
} 