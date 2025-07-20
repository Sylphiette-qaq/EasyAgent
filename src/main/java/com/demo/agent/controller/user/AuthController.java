package com.demo.agent.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.demo.agent.common.Result;
import com.demo.agent.service.user.AuthService;
import com.demo.agent.model.entity.UserEntity;
import com.demo.agent.mapper.UserMapper;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证相关接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserMapper userMapper;

    /** 登录，返回token */
    @PostMapping("/login")
    public Result<String> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        return Result.success(authService.login(username, password));
    }

    /** 校验token是否有效 */
    @GetMapping("/validate")
    public Result<Boolean> validate(@RequestParam("token") String token) {
        return Result.success(authService.validateToken(token));
    }

    /** 注销token */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestParam("token") String token) {
        authService.logout(token);
        return Result.success();
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserEntity userEntity) {
        // 检查用户名是否已存在
        if (userMapper.selectOne(new QueryWrapper<UserEntity>().eq("username", userEntity.getUsername())) != null) {
            return Result.fail("用户名已存在");
        }
        // 密码加密
        userEntity.setPassword(BCrypt.hashpw(userEntity.getPassword()));
        userMapper.insert(userEntity);
        return Result.success();
    }
} 