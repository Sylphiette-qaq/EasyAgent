package com.demo.agent.controller.user;

import com.demo.agent.common.Result;
import com.demo.agent.model.entity.UserEntity;
import com.demo.agent.model.request.UserRequest;
import com.demo.agent.model.response.UserResponse;
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
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(req.getUsername());
        userEntity.setPassword(req.getPassword());
        userEntity.setEmail(req.getEmail());
        userEntity.setPhone(req.getPhone());
        userEntity.setAvatar(req.getAvatar());
        userEntity.setStatus(req.getStatus());
        userService.save(userEntity);
        return Result.success(toResponse(userEntity));
    }

    /** 查询所有 */
    @GetMapping
    public Result<List<UserResponse>> list() {
        List<UserEntity> list = userService.list();
        return Result.success(list.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        UserEntity userEntity = userService.getById(id);
        return Result.success(toResponse(userEntity));
    }

    /** 更新 */
    @PutMapping
    public Result<UserResponse> update(@RequestBody UserRequest req, @RequestParam Long id) {
        UserEntity userEntity = userService.getById(id);
        if (userEntity == null) return Result.fail("用户不存在");
        userEntity.setUsername(req.getUsername());
        userEntity.setPassword(req.getPassword());
        userEntity.setEmail(req.getEmail());
        userEntity.setPhone(req.getPhone());
        userEntity.setAvatar(req.getAvatar());
        userEntity.setStatus(req.getStatus());
        userService.updateById(userEntity);
        return Result.success(toResponse(userEntity));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    private UserResponse toResponse(UserEntity userEntity) {
        if (userEntity == null) return null;
        UserResponse resp = new UserResponse();
        resp.setId(userEntity.getId());
        resp.setUsername(userEntity.getUsername());
        resp.setPassword(userEntity.getPassword());
        resp.setEmail(userEntity.getEmail());
        resp.setPhone(userEntity.getPhone());
        resp.setAvatar(userEntity.getAvatar());
        resp.setStatus(userEntity.getStatus());
        resp.setCreatedAt(userEntity.getCreatedAt());
        resp.setUpdatedAt(userEntity.getUpdatedAt());
        return resp;
    }
} 