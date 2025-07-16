package com.demo.agent.controller.mcp;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.agent.common.Result;
import com.demo.agent.model.entity.Mcp;
import com.demo.agent.model.request.McpRequest;
import com.demo.agent.model.response.McpResponse;
import com.demo.agent.service.mcp.McpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mcp")
public class McpController {
    @Autowired
    private McpService mcpService;

    /** 新增 */
    @PostMapping
    public Result<McpResponse> add(@RequestBody McpRequest req) {
        Mcp mcp = new Mcp();
        BeanUtils.copyProperties(req, mcp);
        mcpService.save(mcp);
        McpResponse resp = new McpResponse();
        BeanUtils.copyProperties(mcp, resp);
        return Result.success(resp);
    }

    /** 分页条件查询 */
    @PostMapping("/page")
    public Result<IPage<McpResponse>> page(@RequestBody McpRequest query) {
        LambdaQueryWrapper<Mcp> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(Mcp::getName, query.getName());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Mcp::getStatus, query.getStatus());
        }
        Page<Mcp> page = new Page<>(query.getPageNum() == null ? 1 : query.getPageNum(), query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<Mcp> mcpPage = mcpService.page(page, wrapper);
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
        Mcp mcp = mcpService.getById(id);
        if (mcp == null) return Result.success(null);
        McpResponse resp = new McpResponse();
        BeanUtils.copyProperties(mcp, resp);
        return Result.success(resp);
    }

    /** 更新 */
    @PutMapping
    public Result<McpResponse> update(@RequestBody McpRequest req, @RequestParam Long id) {
        Mcp mcp = mcpService.getById(id);
        if (mcp == null) return Result.fail("MCP不存在");
        BeanUtils.copyProperties(req, mcp);
        mcpService.updateById(mcp);
        McpResponse resp = new McpResponse();
        BeanUtils.copyProperties(mcp, resp);
        return Result.success(resp);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(mcpService.removeById(id));
    }
} 