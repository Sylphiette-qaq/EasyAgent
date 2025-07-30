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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmm");
    private static final long MAX_16_DIGIT = 9999999999999999L; // 16位最大值
    
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
     * 生成16位以下的唯一数字ID
     */
    private Long generateCustomId(CustomId customId) {
        int length = Math.min(customId.length(), 16); // 确保不超过16位
        
        // 生成基于时间戳的ID，确保16位以下且唯一
        String timestamp = LocalDateTime.now().format(FORMATTER); // 10位：年月日时分
        long sequence = SEQUENCE.getAndIncrement() % 999999; // 最多6位序列号
        
        // 组合时间戳和序列号，确保总长度不超过16位
        String idStr = timestamp + String.format("%06d", sequence);
        
        // 如果指定长度小于16，截取相应长度
        if (length < 16 && idStr.length() > length) {
            idStr = idStr.substring(0, length);
        }
        
        Long id = Long.parseLong(idStr);
        
        // 确保不超过16位最大值
        if (id > MAX_16_DIGIT) {
            id = id % MAX_16_DIGIT;
        }
        
        return id;
    }
    
    /**
     * 生成默认ID（16位以下的时间戳 + 序列号）
     */
    private Long generateDefaultId() {
        // 使用简化的时间戳确保16位以下
        long timestamp = System.currentTimeMillis() / 1000; // 转换为秒级时间戳，减少位数
        long sequence = SEQUENCE.getAndIncrement() % 9999; // 4位序列号
        
        // 组合生成ID，确保不超过16位
        Long id = timestamp * 10000 + sequence;
        
        // 确保不超过16位最大值
        if (id > MAX_16_DIGIT) {
            id = id % MAX_16_DIGIT;
        }
        
        return id;
    }
}