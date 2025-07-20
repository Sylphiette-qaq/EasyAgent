package com.demo.agent.controller.session;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.agent.common.Result;
import com.demo.agent.common.UserContext;
import com.demo.agent.model.entity.SessionEntity;
import com.demo.agent.model.request.SessionRequest;
import com.demo.agent.model.response.SessionResponse;
import com.demo.agent.service.session.SessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/session")
public class SessionController {
    @Autowired
    private SessionService sessionService;

    /** 新增会话 */
    @PostMapping
    public Result<Long> createSession(@RequestBody SessionRequest request) {
        SessionEntity entity = new SessionEntity();
        BeanUtils.copyProperties(request, entity);
        sessionService.save(entity);
        return Result.success(entity.getId());
    }

    /** 分页条件查询 */
    @PostMapping("/page")
    public Result<IPage<SessionResponse>> page(@RequestBody SessionRequest query,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<SessionEntity> wrapper = new LambdaQueryWrapper<>();
        if (query.getUserId() != null) {
            wrapper.eq(SessionEntity::getUserId, query.getUserId());
        }
        if (query.getAgentId() != null) {
            wrapper.eq(SessionEntity::getAgentId, query.getAgentId());
        }
        Page<SessionEntity> page = new Page<>(pageNum, pageSize);
        IPage<SessionEntity> sessionPage = sessionService.page(page, wrapper);
        IPage<SessionResponse> respPage = sessionPage.convert(m -> {
            SessionResponse resp = new SessionResponse();
            BeanUtils.copyProperties(m, resp);
            return resp;
        });
        return Result.success(respPage);
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<SessionResponse> getById(@PathVariable Long id) {
        SessionEntity entity = sessionService.getById(id);
        if (entity == null) return Result.success(null);
        SessionResponse resp = new SessionResponse();
        BeanUtils.copyProperties(entity, resp);
        return Result.success(resp);
    }

    /** 查询全部对话 */
    @GetMapping
    public Result<List<SessionEntity>> getAllSession() {
        LambdaQueryWrapper<SessionEntity> wrapper = new LambdaQueryWrapper<>();
        Long userId = UserContext.getUserId();
        wrapper.eq(SessionEntity::getUserId, userId);
        // 按创建时间升序排序，假设 SessionEntity 有 createTime 字段
        wrapper.orderByDesc(SessionEntity::getCreatedAt);
        List<SessionEntity> list = sessionService.list(wrapper);
        return Result.success(list);
    }

    /** 更新 */
    @PutMapping
    public Result<SessionResponse> update(@RequestBody SessionRequest req, @RequestParam Long id) {
        SessionEntity entity = sessionService.getById(id);
        if (entity == null) return Result.fail("会话不存在");
        BeanUtils.copyProperties(req, entity);
        sessionService.updateById(entity);
        SessionResponse resp = new SessionResponse();
        BeanUtils.copyProperties(entity, resp);
        return Result.success(resp);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sessionService.removeById(id));
    }
} 