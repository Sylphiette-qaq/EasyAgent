package com.demo.agent.service.mcp.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.mapper.McpMapper;
import com.demo.agent.model.entity.Mcp;
import com.demo.agent.service.mcp.McpService;
import org.springframework.stereotype.Service;

@Service
public class McpServiceImpl extends ServiceImpl<McpMapper, Mcp> implements McpService {
}
