package com.demo.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.entity.LlmModel;
import com.demo.agent.mapper.LlmModelMapper;
import com.demo.agent.service.LlmModelService;
import org.springframework.stereotype.Service;

/**
 * LlmModel服务实现
 */
@Service
public class LlmModelServiceImpl extends ServiceImpl<LlmModelMapper, LlmModel> implements LlmModelService {
} 