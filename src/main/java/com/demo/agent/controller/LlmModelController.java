package com.demo.agent.controller;

import com.demo.agent.common.Result;
import com.demo.agent.entity.LlmModel;
import com.demo.agent.service.LlmModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * LlmModel控制器，提供CRUD接口
 */
@RestController
@RequestMapping("/llmModel")
public class LlmModelController {
    @Autowired
    private LlmModelService llmModelService;

    /** 新增 */
    @PostMapping
    public Result<Boolean> add(@RequestBody LlmModel llmModel) {
        return Result.success(llmModelService.save(llmModel));
    }

    /** 查询所有 */
    @GetMapping
    public Result<List<LlmModel>> list() {
        return Result.success(llmModelService.list());
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<LlmModel> getById(@PathVariable Long id) {
        return Result.success(llmModelService.getById(id));
    }

    /** 更新 */
    @PutMapping
    public Result<Boolean> update(@RequestBody LlmModel llmModel) {
        return Result.success(llmModelService.updateById(llmModel));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(llmModelService.removeById(id));
    }
} 