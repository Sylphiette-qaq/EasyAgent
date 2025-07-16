package com.demo.agent.controller.agent;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.agent.common.Result;
import com.demo.agent.common.UserContext;
import com.demo.agent.model.entity.Agent;
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
        Agent agent = new Agent();
        BeanUtils.copyProperties(req, agent);
        agentService.addAgentByUser(agent);
        AgentResponse resp = new AgentResponse();
        BeanUtils.copyProperties(agent, resp);
        return Result.success(resp);
    }

    /** 使用agent */
    @GetMapping("/chat")
    public Result<String> add(@RequestParam("agentId") Long agentId, @RequestParam("userInput") String userInput) {
        String s = agentService.useAgent(agentId, userInput);
        return Result.success(s);
    }

    /** 分页展示用户已有的agent */
    @PostMapping("/getUserAgent")
    public Result<IPage<AgentResponse>> getUserAgent(@RequestBody AgentRequest query) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(Agent::getName, query.getName());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Agent::getStatus, query.getStatus());
        }
        Long userId = UserContext.getUserId();
        wrapper.eq(Agent::getUserId, userId);
        Page<Agent> page = new Page<>(query.getPageNum() == null ? 1 : query.getPageNum(), query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<Agent> agentPage = agentService.page(page, wrapper);
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
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(Agent::getName, query.getName());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Agent::getStatus, query.getStatus());
        }
        Page<Agent> page = new Page<>(query.getPageNum() == null ? 1 : query.getPageNum(), query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<Agent> agentPage = agentService.page(page, wrapper);
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
        Agent agent = agentService.getById(id);
        if (agent == null) return Result.success(null);
        AgentResponse resp = new AgentResponse();
        BeanUtils.copyProperties(agent, resp);
        return Result.success(resp);
    }

    /** 更新 */
    @PutMapping
    public Result<AgentResponse> update(@RequestBody AgentRequest req, @RequestParam Long id) {
        Agent agent = agentService.getById(id);
        if (agent == null) return Result.fail("Agent不存在");
        BeanUtils.copyProperties(req, agent);
        agentService.updateById(agent);
        AgentResponse resp = new AgentResponse();
        BeanUtils.copyProperties(agent, resp);
        return Result.success(resp);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(agentService.removeById(id));
    }
} 