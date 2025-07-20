package com.demo.agent.common;

import java.util.Arrays;

public class Eums {

    public enum McpTypeEnum {
        STDIO(0, "stdio"),
        SSE(1, "sse"),
        HTTP(2, "http");

        private final Integer code;
        private final String description;

        McpTypeEnum(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        // 获取 code
        public Integer getCode() {
            return code;
        }

        // 获取描述
        public String getDescription() {
            return description;
        }

        /**
         * 根据 code 获取 description（使用 for 循环）
         */
        public static String getDescriptionByCode(Integer code) {
            for (McpTypeEnum type : values()) {
                if (type.code.equals(code)) {
                    return type.description;
                }
            }
            return null; // 或抛异常
        }

        /**
         * 根据 description 获取 code（使用 for 循环）
         */
        public static Integer getCodeByDescription(String description) {
            for (McpTypeEnum type : values()) {
                if (type.description.equalsIgnoreCase(description)) {
                    return type.code;
                }
            }
            return 0;
        }

        }
}

