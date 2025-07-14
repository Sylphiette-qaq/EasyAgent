package com.demo.agent.entity;

import lombok.Data;

@Data
public class Message {
    private String role;
    private String content;
    private String reasoningContent;
}