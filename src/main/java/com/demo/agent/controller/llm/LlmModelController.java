package com.demo.agent.controller.llm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.agent.common.Result;
import com.demo.agent.common.UserContext;
import com.demo.agent.model.entity.LlmModel;
import com.demo.agent.model.request.LlmModelRequest;
import com.demo.agent.model.response.LlmModelResponse;
import com.demo.agent.service.ai.LlmModelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/llmModel")
public class LlmModelController {
    @Autowired
    private LlmModelService llmModelService;

    /**
     * 用户新增模型
     * @param req
     * @return
     */
    @PostMapping
    public Result<LlmModelResponse> add(@RequestBody LlmModelRequest req) {
        LlmModel model = new LlmModel();
        BeanUtils.copyProperties(req, model);
        model.setCreateBy(UserContext.getUserId());
        model.setUpdateBy(UserContext.getUserId());
        llmModelService.save(model);
        LlmModelResponse resp = new LlmModelResponse();
        BeanUtils.copyProperties(model, resp);
        return Result.success(resp);
    }

    /** 分页条件查询 */
    @PostMapping("/page")
    public Result<IPage<LlmModelResponse>> page(@RequestBody LlmModelRequest query) {
        LambdaQueryWrapper<LlmModel> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(LlmModel::getName, query.getName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(LlmModel::getStatus, query.getStatus());
        }
        if (query.getUserId() != null) {
            wrapper.eq(LlmModel::getUserId, query.getUserId());
        }
        Page<LlmModel> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<LlmModel> modelPage = llmModelService.page(page, wrapper);
        IPage<LlmModelResponse> respPage = modelPage.convert(model -> {
            LlmModelResponse resp = new LlmModelResponse();
            BeanUtils.copyProperties(model, resp);
            return resp;
        });
        return Result.success(respPage);
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<LlmModelResponse> getById(@PathVariable("id") Long id) {
        LlmModel model = llmModelService.getById(id);
        if (model == null) return Result.success(null);
        LlmModelResponse resp = new LlmModelResponse();
        BeanUtils.copyProperties(model, resp);
        return Result.success(resp);
    }

    /** 更新 */
    @PutMapping
    public Result<LlmModelResponse> update(@RequestBody LlmModelRequest req, @RequestParam Long id) {
        LlmModel model = llmModelService.getById(id);
        if (model == null) return Result.fail("模型不存在");
        BeanUtils.copyProperties(req, model);
        model.setCreateBy(UserContext.getUserId());
        model.setUpdateBy(UserContext.getUserId());
        llmModelService.updateById(model);
        LlmModelResponse resp = new LlmModelResponse();
        BeanUtils.copyProperties(model, resp);
        return Result.success(resp);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(llmModelService.removeById(id));
    }
} 