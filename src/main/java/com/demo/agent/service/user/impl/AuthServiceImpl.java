package com.demo.agent.service.user.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.demo.agent.common.Constants;
import com.demo.agent.model.entity.UserEntity;
import com.demo.agent.mapper.UserMapper;
import com.demo.agent.service.user.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

import static com.demo.agent.common.Constants.TOKEN_PREFIX;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Duration TOKEN_EXPIRE = Duration.ofHours(2);

    @Override
    public String login(String username, String password) {
        UserEntity userEntity = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserEntity>()
                .eq("username", username));
        if (userEntity == null || !BCrypt.checkpw(password, userEntity.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        String key = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userEntity.getId().toString(), TOKEN_EXPIRE);
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