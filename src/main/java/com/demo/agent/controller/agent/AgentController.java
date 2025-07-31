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
import com.demo.agent.model.entity.LlmModelEntity;
import com.demo.agent.model.entity.McpEntity;
import com.demo.agent.service.ai.LlmModelService;
import com.demo.agent.service.mcp.McpService;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/agent")
public class AgentController {
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private LlmModelService llmModelService;
    
    @Autowired
    private McpService mcpService;

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

    /** 使用agent - 流式输出 */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam("agentId") Long agentId,
                                @RequestParam("userInput") String userInput,
                                @RequestParam(value = "sessionId") Long sessionId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        
        try {
            agentService.useAgentStream(agentId, sessionId, userInput, emitter);
        } catch (Exception e) {
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {}
        }
        
        return emitter;
    }

    /** 更改agent所拥有的mcp工具列表 */
    @PostMapping("/changeMcpTool")
    public Result<String> add(@RequestParam("agentId") Long agentId, @RequestParam("mcpTools") String mcpTools) {
        AgentEntity agentEntity = agentService.getById(agentId);
        agentEntity.setMcpIds(mcpTools);
        agentService.updateById(agentEntity);
        agentService.changeAgent(agentId);
        return Result.success();
    }

    /** 更改agent的大语言模型 */
    @PostMapping("/changeLlmModel")
    public Result<String> changeLlmModel(@RequestParam("agentId") Long agentId, @RequestParam("llmModelId") Long llmModelId) {
        AgentEntity agentEntity = agentService.getById(agentId);
        agentEntity.setLlmModelId(llmModelId);
        agentService.updateById(agentEntity);
        agentService.changeAgent(agentId);
        return Result.success();
    }

    /** 更改agent的文件列表 */
    @PostMapping("/changeFile")
    public Result<String> changeFile(
            @RequestParam("agentId") Long agentId, @RequestParam("fileIds") String fileIds) {
        AgentEntity agentEntity = agentService.getById(agentId);
        agentEntity.setFileIds(fileIds);
        agentService.updateById(agentEntity);
        agentService.changeAgent(agentId);
        return Result.success("文件更新成功");
    }

    /** 更改agent的提示词 */
    @PostMapping("/changeSystemPrompt")
    public Result<String> changeSystemPrompt(
            @RequestParam("agentId") Long agentId, @RequestParam("systemPrompt") String systemPrompt) {
        AgentEntity agentEntity = agentService.getById(agentId);
        agentEntity.setSystemPrompt(systemPrompt);
        agentService.updateById(agentEntity);
        agentService.changeAgent(agentId);
        return Result.success("提示词更新成功");
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



    /** 获取创建Agent的选项（模型和MCP工具） */
    @GetMapping("/createOptions")
    public Result<Map<String, Object>> getCreateOptions() {
        Long userId = UserContext.getUserId();
        
        // 获取用户的模型列表
        LambdaQueryWrapper<LlmModelEntity> modelWrapper = new LambdaQueryWrapper<>();
        modelWrapper.eq(LlmModelEntity::getUserId, userId);
        List<LlmModelEntity> modelList = llmModelService.list(modelWrapper);
        
        // 获取用户的MCP工具列表
        LambdaQueryWrapper<McpEntity> mcpWrapper = new LambdaQueryWrapper<>();
        mcpWrapper.eq(McpEntity::getUserId, userId);
        List<McpEntity> mcpList = mcpService.list(mcpWrapper);
        
        Map<String, Object> options = new HashMap<>();
        options.put("models", modelList);
        options.put("mcpTools", mcpList);
        
        return Result.success(options);
    }

}