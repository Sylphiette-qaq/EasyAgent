package com.demo.agent.controller.user;

import com.demo.agent.common.Result;
import com.demo.agent.model.request.UserRequest;
import com.demo.agent.model.response.UserResponse;
import com.demo.agent.model.entity.User;
import com.demo.agent.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

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
    public Result<UserResponse> add(@RequestBody UserRequest req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setAvatar(req.getAvatar());
        user.setStatus(req.getStatus());
        userService.save(user);
        return Result.success(toResponse(user));
    }

    /** 查询所有 */
    @GetMapping
    public Result<List<UserResponse>> list() {
        List<User> list = userService.list();
        return Result.success(list.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(toResponse(user));
    }

    /** 更新 */
    @PutMapping
    public Result<UserResponse> update(@RequestBody UserRequest req, @RequestParam Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.fail("用户不存在");
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setAvatar(req.getAvatar());
        user.setStatus(req.getStatus());
        userService.updateById(user);
        return Result.success(toResponse(user));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    private UserResponse toResponse(User user) {
        if (user == null) return null;
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setPassword(user.getPassword());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setAvatar(user.getAvatar());
        resp.setStatus(user.getStatus());
        resp.setCreatedAt(user.getCreatedAt());
        resp.setUpdatedAt(user.getUpdatedAt());
        return resp;
    }
} 