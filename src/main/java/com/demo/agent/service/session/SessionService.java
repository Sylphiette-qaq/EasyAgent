package com.demo.agent.service.session;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.agent.model.entity.SessionEntity;
import com.demo.agent.model.request.SessionRequest;
import com.demo.agent.model.response.SessionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Session服务接口
 */
@Service
public interface SessionService extends IService<SessionEntity> {

} 