package com.demo.agent.service.agent;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.agent.model.entity.Agent;

public interface AgentService extends IService<Agent> {

    /**
     * 新增Agent
     * @param agent
     */
    public void addAgentByUser(Agent agent);

    /**
     * 调用Agent
     * @param agentId Agent ID
     * @param userInput 用户输入
     * @return Agent输出
     */
    public String useAgent(Long agentId,String userInput);

}