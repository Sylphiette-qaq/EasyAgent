package com.demo.agent.generator;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.demo.agent.annotation.CustomId;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义ID生成器
 * 实现MyBatis-Plus的IdentifierGenerator接口
 */
@Component
public class CustomIdentifierGenerator implements IdentifierGenerator {
    
    private static final AtomicLong SEQUENCE = new AtomicLong(1000);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    @Override
    public Number nextId(Object entity) {
        // 获取实体类的Class对象
        Class<?> entityClass = entity.getClass();
        
        // 查找带有@CustomId注解的字段
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(CustomId.class)) {
                CustomId customId = field.getAnnotation(CustomId.class);
                return generateCustomId(customId);
            }
        }
        
        // 如果没有找到@CustomId注解，使用默认生成方式
        return generateDefaultId();
    }
    
    @Override
    public String nextUUID(Object entity) {
        return String.valueOf(nextId(entity));
    }
    
    /**
     * 根据CustomId注解生成自定义ID
     */
    private Long generateCustomId(CustomId customId) {
        String prefix = customId.prefix();
        int length = customId.length();
        
        if (!prefix.isEmpty()) {
            // 如果有前缀，生成带前缀的ID
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String sequence = String.format("%04d", SEQUENCE.getAndIncrement() % 10000);
            String idStr = prefix + timestamp + sequence;
            
            // 确保长度不超过指定长度
            if (idStr.length() > length) {
                idStr = idStr.substring(0, length);
            }
            
            return Long.parseLong(idStr.replaceAll("[^0-9]", ""));
        } else {
            // 无前缀时生成纯数字ID
            return generateDefaultId();
        }
    }
    
    /**
     * 生成默认ID（时间戳 + 序列号）
     */
    private Long generateDefaultId() {
        // 使用时间戳（毫秒）+ 序列号的方式生成ID
        long timestamp = System.currentTimeMillis();
        long sequence = SEQUENCE.getAndIncrement() % 1000;
        
        // 组合生成19位数字ID
        return timestamp * 1000 + sequence;
    }
}