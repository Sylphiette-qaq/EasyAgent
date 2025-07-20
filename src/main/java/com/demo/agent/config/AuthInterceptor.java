package com.demo.agent.config;

import com.demo.agent.common.ResultCode;
import com.demo.agent.common.UserContext;
import com.demo.agent.service.user.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.demo.agent.common.Constants.TOKEN_EXPIRE;
import static com.demo.agent.common.Constants.TOKEN_PREFIX;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private AuthService authService;
    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        // 登录、注册、静态资源等不拦截
        if (uri.startsWith("/auth/login") || uri.startsWith("/auth/validate") || uri.startsWith("/auth/logout") || uri.startsWith("/auth/register") || uri.startsWith("/error")) {
            return true;
        }
        String token = request.getHeader("token");
        if (token == null || !authService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format("{\"code\":%d,\"msg\":\"%s\"}", ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg()));
            return false;
        }
        // token续期
        String key = TOKEN_PREFIX + token;
        redisTemplate.expire(key, TOKEN_EXPIRE);
        // 取出用户id并存入ThreadLocal
        String userId = redisTemplate.opsForValue().get(key);
        if (userId != null) {
            UserContext.setUserId(Long.valueOf(userId));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
} 