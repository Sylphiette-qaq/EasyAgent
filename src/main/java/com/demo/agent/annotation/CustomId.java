package com.demo.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义ID生成注解
 * 用于标识需要使用自定义ID生成器的字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomId {
    /**
     * ID生成器类型
     * @return 生成器类型
     */
    String value() default "default";
    
    /**
     * ID前缀
     * @return 前缀字符串
     */
    String prefix() default "";
    
    /**
     * ID长度
     * @return ID长度
     */
    int length() default 19;
}