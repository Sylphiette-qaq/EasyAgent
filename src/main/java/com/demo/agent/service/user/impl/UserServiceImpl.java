package com.demo.agent.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.model.entity.UserEntity;
import com.demo.agent.mapper.UserMapper;
import com.demo.agent.service.user.UserService;
import org.springframework.stereotype.Service;

/**
 * User服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
} 