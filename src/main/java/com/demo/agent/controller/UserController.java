package com.demo.agent.controller;

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
    public boolean add(@RequestBody User user) {
        return userService.save(user);
    }

    /** 查询所有 */
    @GetMapping
    public List<User> list() {
        return userService.list();
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    /** 更新 */
    @PutMapping
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return userService.removeById(id);
    }
} 