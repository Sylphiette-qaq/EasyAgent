package com.demo.agent.model.response;

import lombok.Data;

@Data
public class SessionResponse {

    /** 会话名称 */
    private String name;

    /** 会话id */
    private Long id;

    /** 使用的大语言模型id */
    private Long agentId;

    /** 本次对话的用户id */
    private Long userId;

    /** 会话内容 */
    private String content;
} 