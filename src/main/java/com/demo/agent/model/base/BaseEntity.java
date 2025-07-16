package com.demo.agent.model.base;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 基础实体类，包含创建/更新信息
 */
@Data
public class BaseEntity {
    /** 创建人id */
    private Long createBy;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新人id */
    private Long updateBy;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    private Integer pageNum;

    private Integer pageSize;
} 