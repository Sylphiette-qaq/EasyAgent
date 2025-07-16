package com.demo.agent.service.ai.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.model.entity.LlmModel;
import com.demo.agent.mapper.LlmModelMapper;
import com.demo.agent.service.ai.LlmModelService;
import org.springframework.stereotype.Service;

/**
 * LlmModel服务实现
 */
@Service
public class LlmModelServiceImpl extends ServiceImpl<LlmModelMapper, LlmModel> implements LlmModelService {
} 