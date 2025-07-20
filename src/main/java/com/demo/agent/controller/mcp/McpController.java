package com.demo.agent.controller.mcp;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.agent.common.Result;
import com.demo.agent.model.entity.McpEntity;
import com.demo.agent.model.entity.McpToolConfig;
import com.demo.agent.model.request.McpRequest;
import com.demo.agent.model.response.McpResponse;
import com.demo.agent.service.mcp.McpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mcp")
public class McpController {
    @Autowired
    private McpService mcpService;

    @PostMapping("/upload")
    public Result<Long> uploadMcpJson(@RequestBody McpToolConfig config) {
        try {
            // TODO: 校验 & 注册
            System.out.println("收到 MCP 工具：" + config.getMcpServers().keySet());
            mcpService.registerMcp(config);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }


    /** 分页条件查询 */
    @PostMapping("/page")
    public Result<IPage<McpResponse>> page(@RequestBody McpRequest query) {
        LambdaQueryWrapper<McpEntity> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(McpEntity::getName, query.getName());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(McpEntity::getStatus, query.getStatus());
        }
        Page<McpEntity> page = new Page<>(query.getPageNum() == null ? 1 : query.getPageNum(), query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<McpEntity> mcpPage = mcpService.page(page, wrapper);
        IPage<McpResponse> respPage = mcpPage.convert(m -> {
            McpResponse resp = new McpResponse();
            BeanUtils.copyProperties(m, resp);
            return resp;
        });
        return Result.success(respPage);
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<McpResponse> getById(@PathVariable Long id) {
        McpEntity mcpEntity = mcpService.getById(id);
        if (mcpEntity == null) return Result.success(null);
        McpResponse resp = new McpResponse();
        BeanUtils.copyProperties(mcpEntity, resp);
        return Result.success(resp);
    }

    /** 更新 */
    @PutMapping
    public Result<McpResponse> update(@RequestBody McpRequest req, @RequestParam Long id) {
        McpEntity mcpEntity = mcpService.getById(id);
        if (mcpEntity == null) return Result.fail("MCP不存在");
        BeanUtils.copyProperties(req, mcpEntity);
        mcpService.updateById(mcpEntity);
        McpResponse resp = new McpResponse();
        BeanUtils.copyProperties(mcpEntity, resp);
        return Result.success(resp);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(mcpService.removeById(id));
    }
} 