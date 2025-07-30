package com.demo.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.demo.agent.annotation.CustomId;
import com.demo.agent.model.base.BaseEntity;
import lombok.Data;

@Data
@TableName("session")
public class SessionEntity extends BaseEntity {

    /** 会话id */
    @TableId(type = IdType.ASSIGN_ID)
    @CustomId(length = 16)
    private Long id;

    /** 会话名称 */
    private String name;

    /** 使用的大语言模型id */
    private Long agentId;

    /** 本次对话的用户id */
    private Long userId;


    /** 会话内容 */
    private String content;
}
