package com.demo.agent.service.agent;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.agent.model.entity.AgentEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    /**
     * 调用Agent - 流式输出
     * @param agentId Agent ID
     * @param sessionId 会话ID
     * @param userInput 用户输入
     * @param emitter SSE发射器
     */
    public void useAgentStream(Long agentId, Long sessionId, String userInput, SseEmitter emitter);

    void changeAgent(Long agentId);

    /**
     * 知识库文件变更后，若当前用户已缓存该 Agent 实例则重建以启用/更新 RAG
     */
    void refreshAgentAfterKnowledgeChange(Long agentId);
}