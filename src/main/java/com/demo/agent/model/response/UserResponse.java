package com.demo.agent.model.response;

import com.demo.agent.model.base.BaseEntity;
import lombok.Data;

/**
 * 用户表-返回类
 */
@Data
public class UserResponse extends BaseEntity {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
} 