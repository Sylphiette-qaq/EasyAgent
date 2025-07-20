package com.demo.agent.model.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 消息队列传输实体
 */
@Data
public class RabbitMqTransportEntity {

    /**
     * 消息id
     */
    private Long id;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息时间
     */
    private Date dateTime;
}
