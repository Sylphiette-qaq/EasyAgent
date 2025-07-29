package com.demo.agent.test;

import com.demo.agent.generator.CustomIdentifierGenerator;
import com.demo.agent.model.entity.AgentEntity;
import org.springframework.stereotype.Component;

/**
 * 自定义ID生成器测试类
 */
@Component
public class CustomIdGeneratorTest {
    
    /**
     * 测试自定义ID生成
     */
    public void testCustomIdGeneration() {
        CustomIdentifierGenerator generator = new CustomIdentifierGenerator();
        AgentEntity agent = new AgentEntity();
        
        // 生成多个ID进行测试
        for (int i = 0; i < 5; i++) {
            Number id = generator.nextId(agent);
            String uuid = generator.nextUUID(agent);
            
            System.out.println("Generated ID " + (i + 1) + ": " + id);
            System.out.println("Generated UUID " + (i + 1) + ": " + uuid);
            System.out.println("---");
            
            // 添加小延迟以确保时间戳不同
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 测试ID唯一性
     */
    public void testIdUniqueness() {
        CustomIdentifierGenerator generator = new CustomIdentifierGenerator();
        AgentEntity agent = new AgentEntity();
        
        java.util.Set<Number> generatedIds = new java.util.HashSet<>();
        int testCount = 1000;
        
        for (int i = 0; i < testCount; i++) {
            Number id = generator.nextId(agent);
            if (!generatedIds.add(id)) {
                System.out.println("发现重复ID: " + id);
                return;
            }
        }
        
        System.out.println("生成了 " + testCount + " 个唯一ID，无重复");
    }
}