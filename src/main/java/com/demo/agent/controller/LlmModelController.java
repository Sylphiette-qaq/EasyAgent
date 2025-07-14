package com.demo.agent.controller;

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
    public boolean add(@RequestBody LlmModel llmModel) {
        return llmModelService.save(llmModel);
    }

    /** 查询所有 */
    @GetMapping
    public List<LlmModel> list() {
        return llmModelService.list();
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public LlmModel getById(@PathVariable Long id) {
        return llmModelService.getById(id);
    }

    /** 更新 */
    @PutMapping
    public boolean update(@RequestBody LlmModel llmModel) {
        return llmModelService.updateById(llmModel);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return llmModelService.removeById(id);
    }
} 