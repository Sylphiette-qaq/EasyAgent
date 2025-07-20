package com.demo.agent.service.agent;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.agent.model.entity.AgentEntity;

public interface AgentService extends IService<AgentEntity> {

    /**
     * 新增Agent
     * @param agentEntity
     */
    public void addAgentByUser(AgentEntity agentEntity);

    /**
     * 调用Agent
     * @param agentId Agent ID
     * @param userInput 用户输入
     * @return Agent输出
     */
    public String useAgent(Long agentId,Long sessionId,String userInput);

}