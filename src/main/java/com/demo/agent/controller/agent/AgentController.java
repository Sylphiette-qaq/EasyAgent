package com.demo.agent.controller.agent;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.agent.common.Result;
import com.demo.agent.common.UserContext;
import com.demo.agent.model.entity.AgentEntity;
import com.demo.agent.model.request.AgentRequest;
import com.demo.agent.model.response.AgentResponse;
import com.demo.agent.service.agent.AgentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AgentController {
    @Autowired
    private AgentService agentService;

    /** 新增 */
    @PostMapping
    public Result<AgentResponse> add(@RequestBody AgentRequest req) {
        AgentEntity agentEntity = new AgentEntity();
        BeanUtils.copyProperties(req, agentEntity);
        agentService.addAgentByUser(agentEntity);
        AgentResponse resp = new AgentResponse();
        BeanUtils.copyProperties(agentEntity, resp);
        return Result.success(resp);
    }

    /** 使用agent */
    @GetMapping("/chat")
    public Result<String> add(@RequestParam("agentId") Long agentId, @RequestParam("userInput") String userInput
    ,@RequestParam(value = "sessionId")  Long sessionId) {
        String s = agentService.useAgent(agentId, sessionId, userInput);
        return Result.success(s);
    }

    /** 分页展示用户已有的agent */
    @PostMapping("/getUserAgent")
    public Result<IPage<AgentResponse>> getUserAgent(@RequestBody AgentRequest query) {
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(AgentEntity::getName, query.getName());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(AgentEntity::getStatus, query.getStatus());
        }
        Long userId = UserContext.getUserId();
        wrapper.eq(AgentEntity::getUserId, userId);
        Page<AgentEntity> page = new Page<>(query.getPageNum() == null ? 1 : query.getPageNum(), query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<AgentEntity> agentPage = agentService.page(page, wrapper);
        IPage<AgentResponse> respPage = agentPage.convert(a -> {
            AgentResponse resp = new AgentResponse();
            BeanUtils.copyProperties(a, resp);
            return resp;
        });
        return Result.success(respPage);
    }



    /** 分页条件查询 */
    @PostMapping("/page")
    public Result<IPage<AgentResponse>> page(@RequestBody AgentRequest query) {
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(AgentEntity::getName, query.getName());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(AgentEntity::getStatus, query.getStatus());
        }
        Page<AgentEntity> page = new Page<>(query.getPageNum() == null ? 1 : query.getPageNum(), query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<AgentEntity> agentPage = agentService.page(page, wrapper);
        IPage<AgentResponse> respPage = agentPage.convert(a -> {
            AgentResponse resp = new AgentResponse();
            BeanUtils.copyProperties(a, resp);
            return resp;
        });
        return Result.success(respPage);
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<AgentResponse> getById(@PathVariable Long id) {
        AgentEntity agentEntity = agentService.getById(id);
        if (agentEntity == null) return Result.success(null);
        AgentResponse resp = new AgentResponse();
        BeanUtils.copyProperties(agentEntity, resp);
        return Result.success(resp);
    }

    /** 更新 */
    @PutMapping
    public Result<AgentResponse> update(@RequestBody AgentRequest req, @RequestParam Long id) {
        AgentEntity agentEntity = agentService.getById(id);
        if (agentEntity == null) return Result.fail("Agent不存在");
        BeanUtils.copyProperties(req, agentEntity);
        agentService.updateById(agentEntity);
        AgentResponse resp = new AgentResponse();
        BeanUtils.copyProperties(agentEntity, resp);
        return Result.success(resp);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(agentService.removeById(id));
    }
} 