package com.demo.agent.entity;

import lombok.Data;
import java.util.List;

/**
 * AI对话响应封装类，对应大模型返回结构
 */
@Data
public class AiChatResponse {
    /** 响应ID */
    private String id;
    /** 对象类型 */
    private String object;
    /** 创建时间戳 */
    private Long created;
    /** 模型名称 */
    private String model;
    /** 选项列表 */
    private List<Choice> choices;
    /** 使用情况 */
    private Usage usage;
    /** 系统指纹 */
    private String systemFingerprint;

    @Data
    public static class Choice {
        private Integer index;
        private Message message;
        private String finishReason;
    }

    /**
     * 消息内容封装类
     */
   private Message message;

    @Data
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        private CompletionTokensDetails completionTokensDetails;
    }

    @Data
    public static class CompletionTokensDetails {
        private String reasoningTokens;
    }
} 