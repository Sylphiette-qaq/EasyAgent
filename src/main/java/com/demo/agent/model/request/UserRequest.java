package com.demo.agent.model.request;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;

/**
 * 用户表-请求类
 */
@Data
public class UserRequest extends BaseEntity {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
} 