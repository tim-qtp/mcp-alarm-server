package com.qtp.mcpserver.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    
    /**
     * 告警ID
     */
    private String id;
    
    /**
     * 告警名称
     */
    private String name;
    
    /**
     * 告警类型
     */
    private String type;
    
    /**
     * 告警级别 (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String level;
    
    /**
     * 告警状态 (ACTIVE, RESOLVED, PENDING)
     */
    private String status;
    
    /**
     * 告警描述
     */
    private String description;
    
    /**
     * 告警来源
     */
    private String source;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 告警规则
     */
    private String rule;
    
    /**
     * 告警值
     */
    private String value;
    
    /**
     * 告警阈值
     */
    private String threshold;
} 