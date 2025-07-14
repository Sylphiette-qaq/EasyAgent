package com.demo.agent.controller;

import com.demo.agent.common.Result;
import com.demo.agent.entity.User;
import com.demo.agent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * User控制器，提供CRUD接口
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /** 新增 */
    @PostMapping
    public Result<Boolean> add(@RequestBody User user) {
        return Result.success(userService.save(user));
    }

    /** 查询所有 */
    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /** 更新 */
    @PutMapping
    public Result<Boolean> update(@RequestBody User user) {
        return Result.success(userService.updateById(user));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }
} 