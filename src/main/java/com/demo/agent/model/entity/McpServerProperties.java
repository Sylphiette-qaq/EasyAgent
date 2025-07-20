package com.demo.agent.model.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class McpServerProperties {
    private String type;              // stdio / sse / http

    // stdio 通用字段
    private String command;
    private List<String> args;
    private Map<String, String> env;

    // sse/http 通用字段
    private String url;
    private String method;            // http用
    private Map<String, String> headers;
}