package com.demo.agent.service.user;

public interface AuthService {
    /**
     * 登录，返回token
     */
    String login(String username, String password);

    /**
     * 校验token是否有效
     */
    boolean validateToken(String token);

    /**
     * 注销token
     */
    void logout(String token);
} 