package com.qtp.mcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.qtp.mcpserver.entity.Alert;
import com.qtp.mcpserver.storage.AlertStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlertTool {
    
    // 使用共享的告警存储
    private final AlertStorage alertStorage = AlertStorage.getInstance();

    @Tool(description = "查询某个告警的详细信息")
    public String queryAlert(@ToolParam(description = "告警ID") String alertId) {
        try {
            if (StrUtil.isBlank(alertId)) {
                return "错误：告警ID不能为空";
            }
            
            Alert alert = alertStorage.getAlert(alertId);
            if (alert == null) {
                return "未找到ID为 " + alertId + " 的告警";
            }
            
            return "告警详情：\n" + JSONUtil.toJsonPrettyStr(alert);
        } catch (Exception e) {
            log.error("查询告警失败", e);
            return "查询告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "查询告警信息列表，支持按状态、级别、类型筛选")
    public String queryAlertList(
            @ToolParam(description = "告警状态筛选 (ACTIVE, RESOLVED, PENDING)，可选") String status,
            @ToolParam(description = "告警级别筛选 (LOW, MEDIUM, HIGH, CRITICAL)，可选") String level,
            @ToolParam(description = "告警类型筛选，可选") String type,
            @ToolParam(description = "每页数量，默认10") Integer pageSize,
            @ToolParam(description = "页码，默认1") Integer pageNum) {
        try {
            // 设置默认值
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            if (pageNum == null || pageNum <= 0) {
                pageNum = 1;
            }
            
            List<Alert> alerts = new ArrayList<>(alertStorage.getAllAlerts().values());
            
            // 筛选条件
            if (StrUtil.isNotBlank(status)) {
                alerts = alerts.stream()
                        .filter(alert -> status.equalsIgnoreCase(alert.getStatus()))
                        .collect(Collectors.toList());
            }
            
            if (StrUtil.isNotBlank(level)) {
                alerts = alerts.stream()
                        .filter(alert -> level.equalsIgnoreCase(alert.getLevel()))
                        .collect(Collectors.toList());
            }
            
            if (StrUtil.isNotBlank(type)) {
                alerts = alerts.stream()
                        .filter(alert -> type.equalsIgnoreCase(alert.getType()))
                        .collect(Collectors.toList());
            }
            
            // 分页处理
            int total = alerts.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            
            if (start >= total) {
                return "告警列表（第" + pageNum + "页，共0条）：[]";
            }
            
            List<Alert> pageAlerts = alerts.subList(start, end);
            
            return "告警列表（第" + pageNum + "页，共" + total + "条）：\n" + 
                   JSONUtil.toJsonPrettyStr(pageAlerts);
        } catch (Exception e) {
            log.error("查询告警列表失败", e);
            return "查询告警列表失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "批量插入告警列表")
    public String batchInsertAlerts(@ToolParam(description = "告警列表JSON字符串，格式：[{\"name\":\"告警名称\",\"type\":\"告警类型\",\"level\":\"告警级别\",\"status\":\"告警状态\",\"description\":\"告警描述\",\"source\":\"告警来源\",\"rule\":\"告警规则\",\"value\":\"告警值\",\"threshold\":\"告警阈值\"}]") String alertsJson) {
        try {
            if (StrUtil.isBlank(alertsJson)) {
                return "错误：告警列表JSON不能为空";
            }
            
            // 解析JSON
            List<Alert> alerts = JSONUtil.toList(alertsJson, Alert.class);
            List<Alert> insertedAlerts = new ArrayList<>();
            
            for (Alert alert : alerts) {
                // 验证必填字段
                if (StrUtil.isBlank(alert.getName()) || StrUtil.isBlank(alert.getType()) || 
                    StrUtil.isBlank(alert.getLevel()) || StrUtil.isBlank(alert.getStatus())) {
                    continue; // 跳过无效数据
                }
                
                // 设置ID和时间
                alert.setId(UUID.randomUUID().toString());
                alert.setCreateTime(LocalDateTime.now());
                alert.setUpdateTime(LocalDateTime.now());
                
                // 设置默认值
                if (StrUtil.isBlank(alert.getDescription())) {
                    alert.setDescription("无描述");
                }
                if (StrUtil.isBlank(alert.getSource())) {
                    alert.setSource("系统");
                }
                
                // 保存到存储
                alertStorage.saveAlert(alert);
                insertedAlerts.add(alert);
            }
            
            return "批量插入成功，共插入 " + insertedAlerts.size() + " 条告警：\n" + 
                   JSONUtil.toJsonPrettyStr(insertedAlerts);
        } catch (Exception e) {
            log.error("批量插入告警失败", e);
            return "批量插入告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "插入一条告警记录")
    public String insertAlert(
            @ToolParam(description = "告警名称") String name,
            @ToolParam(description = "告警类型") String type,
            @ToolParam(description = "告警级别 (LOW, MEDIUM, HIGH, CRITICAL)") String level,
            @ToolParam(description = "告警状态 (ACTIVE, RESOLVED, PENDING)") String status,
            @ToolParam(description = "告警描述，可选") String description,
            @ToolParam(description = "告警来源，可选") String source,
            @ToolParam(description = "告警规则，可选") String rule,
            @ToolParam(description = "告警值，可选") String value,
            @ToolParam(description = "告警阈值，可选") String threshold) {
        try {
            // 验证必填字段
            if (StrUtil.isBlank(name) || StrUtil.isBlank(type) || 
                StrUtil.isBlank(level) || StrUtil.isBlank(status)) {
                return "错误：告警名称、类型、级别、状态不能为空";
            }
            
            // 创建告警对象
            Alert alert = new Alert();
            alert.setId(UUID.randomUUID().toString());
            alert.setName(name);
            alert.setType(type);
            alert.setLevel(level);
            alert.setStatus(status);
            alert.setDescription(StrUtil.isBlank(description) ? "无描述" : description);
            alert.setSource(StrUtil.isBlank(source) ? "系统" : source);
            alert.setRule(rule);
            alert.setValue(value);
            alert.setThreshold(threshold);
            alert.setCreateTime(LocalDateTime.now());
            alert.setUpdateTime(LocalDateTime.now());
            
            // 保存到存储
            alertStorage.saveAlert(alert);
            
            return "告警插入成功：\n" + JSONUtil.toJsonPrettyStr(alert);
        } catch (Exception e) {
            log.error("插入告警失败", e);
            return "插入告警失败：" + e.getMessage();
        }
    }
} 