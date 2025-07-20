package com.demo.agent.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SessionRequest {

    /** 会话名称 */
    private String name;

    /** 使用的大语言模型id */
    @NotNull(message = "agentId不能为空")
    private Long agentId;

    /** 本次对话的用户id */
    @NotNull(message = "userId不能为空")
    private Long userId;

    /** 会话内容 */
    private String content;
}