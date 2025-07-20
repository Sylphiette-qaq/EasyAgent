package com.demo.agent.common;

import java.time.Duration;

public class Constants {

    /**
     * 权限校验前缀
     */
    public static final String TOKEN_PREFIX = "auth:token:";


    /**
     * 权限校验过期时间
     */
    public static final Duration TOKEN_EXPIRE = Duration.ofHours(2);

    /**
     * 消息队列交换机名称
     */
    public static final String EXCHANGE_NAME = "message_memory_exchange";

    /**
     * 消息队列路由键
     */
    public static final String ROUTING_KEY = "message_memory_routing_key";

    /**
     * 消息队列名称
     */
    public static final String QUEUE_NAME = "message_memory_queue";

    /**
     * 权限校验前缀
     */
    public static final String MESSAGE_MEMORY_PREFIX = "message:memory:";

}
