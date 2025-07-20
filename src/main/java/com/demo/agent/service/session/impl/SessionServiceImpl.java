package com.demo.agent.service.session.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.mapper.SessionMapper;
import com.demo.agent.model.entity.SessionEntity;
import com.demo.agent.model.request.SessionRequest;
import com.demo.agent.model.response.SessionResponse;
import com.demo.agent.service.session.SessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Session服务实现
 */
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, SessionEntity> implements SessionService {


} 