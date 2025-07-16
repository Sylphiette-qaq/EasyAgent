package com.demo.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.demo.agent.model.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户表
 */
@Data
@TableName("user")
public class User extends BaseEntity {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名不能超过50字符")
    private String username;

    /** 加密密码 */
    @NotBlank(message = "密码不能为空")
    @Size(max = 100, message = "密码不能超过100字符")
    private String password;

    /** 邮箱 */
    @Size(max = 100, message = "邮箱不能超过100字符")
    private String email;

    /** 手机号 */
    @Size(max = 20, message = "手机号不能超过20字符")
    private String phone;

    /** 头像URL */
    @Size(max = 255, message = "头像URL不能超过255字符")
    private String avatar;

    /** 状态（1:正常 0:禁用） */
    @NotBlank(message = "状态不能为空")
    private Integer status;
} 