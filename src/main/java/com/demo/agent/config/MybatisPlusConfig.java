package com.demo.agent.config;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.demo.agent.generator.CustomIdentifierGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 */
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 配置自定义ID生成器
     */
    @Bean
    public GlobalConfig globalConfig(CustomIdentifierGenerator customIdentifierGenerator) {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setIdentifierGenerator(customIdentifierGenerator);
        return globalConfig;
    }
}