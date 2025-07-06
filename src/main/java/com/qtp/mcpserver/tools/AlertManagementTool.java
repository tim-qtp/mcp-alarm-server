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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlertManagementTool {
    
    // 使用共享的告警存储
    private final AlertStorage alertStorage = AlertStorage.getInstance();
    
    // 时间格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Tool(description = "更新告警信息")
    public String updateAlert(
            @ToolParam(description = "告警ID") String alertId,
            @ToolParam(description = "告警名称，可选") String name,
            @ToolParam(description = "告警类型，可选") String type,
            @ToolParam(description = "告警级别 (LOW, MEDIUM, HIGH, CRITICAL)，可选") String level,
            @ToolParam(description = "告警状态 (ACTIVE, RESOLVED, PENDING)，可选") String status,
            @ToolParam(description = "告警描述，可选") String description,
            @ToolParam(description = "告警来源，可选") String source,
            @ToolParam(description = "告警规则，可选") String rule,
            @ToolParam(description = "告警值，可选") String value,
            @ToolParam(description = "告警阈值，可选") String threshold) {
        try {
            if (StrUtil.isBlank(alertId)) {
                return "错误：告警ID不能为空";
            }
            
            Alert alert = alertStorage.getAlert(alertId);
            if (alert == null) {
                return "未找到ID为 " + alertId + " 的告警";
            }
            
            // 更新非空字段
            if (StrUtil.isNotBlank(name)) alert.setName(name);
            if (StrUtil.isNotBlank(type)) alert.setType(type);
            if (StrUtil.isNotBlank(level)) alert.setLevel(level);
            if (StrUtil.isNotBlank(status)) alert.setStatus(status);
            if (StrUtil.isNotBlank(description)) alert.setDescription(description);
            if (StrUtil.isNotBlank(source)) alert.setSource(source);
            if (StrUtil.isNotBlank(rule)) alert.setRule(rule);
            if (StrUtil.isNotBlank(value)) alert.setValue(value);
            if (StrUtil.isNotBlank(threshold)) alert.setThreshold(threshold);
            
            alert.setUpdateTime(LocalDateTime.now());
            
            return "告警更新成功：\n" + JSONUtil.toJsonPrettyStr(alert);
        } catch (Exception e) {
            log.error("更新告警失败", e);
            return "更新告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "删除告警")
    public String deleteAlert(@ToolParam(description = "告警ID") String alertId) {
        try {
            if (StrUtil.isBlank(alertId)) {
                return "错误：告警ID不能为空";
            }
            
                         Alert alert = alertStorage.removeAlert(alertId);
            if (alert == null) {
                return "未找到ID为 " + alertId + " 的告警";
            }
            
            return "告警删除成功：" + alert.getName() + " (ID: " + alertId + ")";
        } catch (Exception e) {
            log.error("删除告警失败", e);
            return "删除告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "批量删除告警")
    public String batchDeleteAlerts(@ToolParam(description = "告警ID列表，用逗号分隔") String alertIds) {
        try {
            if (StrUtil.isBlank(alertIds)) {
                return "错误：告警ID列表不能为空";
            }
            
            String[] ids = alertIds.split(",");
            List<String> deletedAlerts = new ArrayList<>();
            List<String> notFoundAlerts = new ArrayList<>();
            
            for (String id : ids) {
                String trimmedId = id.trim();
                                 Alert alert = alertStorage.removeAlert(trimmedId);
                if (alert != null) {
                    deletedAlerts.add(alert.getName() + " (ID: " + trimmedId + ")");
                } else {
                    notFoundAlerts.add(trimmedId);
                }
            }
            
            StringBuilder result = new StringBuilder();
            result.append("批量删除结果：\n");
            result.append("成功删除 ").append(deletedAlerts.size()).append(" 条告警：\n");
            deletedAlerts.forEach(alert -> result.append("- ").append(alert).append("\n"));
            
            if (!notFoundAlerts.isEmpty()) {
                result.append("未找到的告警ID：\n");
                notFoundAlerts.forEach(id -> result.append("- ").append(id).append("\n"));
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("批量删除告警失败", e);
            return "批量删除告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "解决告警（将状态设置为RESOLVED）")
    public String resolveAlert(@ToolParam(description = "告警ID") String alertId) {
        try {
            if (StrUtil.isBlank(alertId)) {
                return "错误：告警ID不能为空";
            }
            
            Alert alert = alertStorage.getAlert(alertId);
            if (alert == null) {
                return "未找到ID为 " + alertId + " 的告警";
            }
            
            alert.setStatus("RESOLVED");
            alert.setUpdateTime(LocalDateTime.now());
            
            return "告警已解决：" + alert.getName() + " (ID: " + alertId + ")";
        } catch (Exception e) {
            log.error("解决告警失败", e);
            return "解决告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "激活告警（将状态设置为ACTIVE）")
    public String activateAlert(@ToolParam(description = "告警ID") String alertId) {
        try {
            if (StrUtil.isBlank(alertId)) {
                return "错误：告警ID不能为空";
            }
            
            Alert alert = alertStorage.getAlert(alertId);
            if (alert == null) {
                return "未找到ID为 " + alertId + " 的告警";
            }
            
            alert.setStatus("ACTIVE");
            alert.setUpdateTime(LocalDateTime.now());
            
            return "告警已激活：" + alert.getName() + " (ID: " + alertId + ")";
        } catch (Exception e) {
            log.error("激活告警失败", e);
            return "激活告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "获取告警统计信息")
    public String getAlertStatistics() {
        try {
                         Collection<Alert> alerts = alertStorage.getAllAlerts().values();
            
            // 按状态统计
            Map<String, Long> statusStats = alerts.stream()
                    .collect(Collectors.groupingBy(Alert::getStatus, Collectors.counting()));
            
            // 按级别统计
            Map<String, Long> levelStats = alerts.stream()
                    .collect(Collectors.groupingBy(Alert::getLevel, Collectors.counting()));
            
            // 按类型统计
            Map<String, Long> typeStats = alerts.stream()
                    .collect(Collectors.groupingBy(Alert::getType, Collectors.counting()));
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("总告警数", alerts.size());
            statistics.put("按状态统计", statusStats);
            statistics.put("按级别统计", levelStats);
            statistics.put("按类型统计", typeStats);
            
            return "告警统计信息：\n" + JSONUtil.toJsonPrettyStr(statistics);
        } catch (Exception e) {
            log.error("获取告警统计失败", e);
            return "获取告警统计失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "全文搜索告警")
    public String searchAlerts(
            @ToolParam(description = "搜索关键词") String keyword,
            @ToolParam(description = "每页数量，默认10") Integer pageSize,
            @ToolParam(description = "页码，默认1") Integer pageNum) {
        try {
            if (StrUtil.isBlank(keyword)) {
                return "错误：搜索关键词不能为空";
            }
            
            if (pageSize == null || pageSize <= 0) pageSize = 10;
            if (pageNum == null || pageNum <= 0) pageNum = 1;
            
                         List<Alert> matchedAlerts = alertStorage.getAllAlerts().values().stream()
                    .filter(alert -> 
                        (alert.getName() != null && alert.getName().contains(keyword)) ||
                        (alert.getDescription() != null && alert.getDescription().contains(keyword)) ||
                        (alert.getType() != null && alert.getType().contains(keyword)) ||
                        (alert.getSource() != null && alert.getSource().contains(keyword))
                    )
                    .collect(Collectors.toList());
            
            // 分页处理
            int total = matchedAlerts.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            
            if (start >= total) {
                return "搜索结果（关键词：" + keyword + "，第" + pageNum + "页，共0条）：[]";
            }
            
            List<Alert> pageAlerts = matchedAlerts.subList(start, end);
            
            return "搜索结果（关键词：" + keyword + "，第" + pageNum + "页，共" + total + "条）：\n" + 
                   JSONUtil.toJsonPrettyStr(pageAlerts);
        } catch (Exception e) {
            log.error("搜索告警失败", e);
            return "搜索告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "按时间范围查询告警")
    public String getAlertsByTimeRange(
            @ToolParam(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss") String startTime,
            @ToolParam(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss") String endTime,
            @ToolParam(description = "时间字段类型 (CREATE, UPDATE)，默认CREATE") String timeField) {
        try {
            if (StrUtil.isBlank(startTime) || StrUtil.isBlank(endTime)) {
                return "错误：开始时间和结束时间不能为空";
            }
            
            LocalDateTime start, end;
            try {
                start = LocalDateTime.parse(startTime, DATE_FORMATTER);
                end = LocalDateTime.parse(endTime, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                return "错误：时间格式不正确，请使用 yyyy-MM-dd HH:mm:ss 格式";
            }
            
            if (start.isAfter(end)) {
                return "错误：开始时间不能晚于结束时间";
            }
            
            boolean useCreateTime = !"UPDATE".equalsIgnoreCase(timeField);
            
                         List<Alert> filteredAlerts = alertStorage.getAllAlerts().values().stream()
                    .filter(alert -> {
                        LocalDateTime targetTime = useCreateTime ? alert.getCreateTime() : alert.getUpdateTime();
                        return targetTime != null && 
                               !targetTime.isBefore(start) && 
                               !targetTime.isAfter(end);
                    })
                    .collect(Collectors.toList());
            
            return "时间范围查询结果（" + startTime + " 至 " + endTime + "，共" + filteredAlerts.size() + "条）：\n" + 
                   JSONUtil.toJsonPrettyStr(filteredAlerts);
        } catch (Exception e) {
            log.error("按时间范围查询告警失败", e);
            return "按时间范围查询告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "升级告警级别")
    public String escalateAlert(@ToolParam(description = "告警ID") String alertId) {
        try {
            if (StrUtil.isBlank(alertId)) {
                return "错误：告警ID不能为空";
            }
            
            Alert alert = alertStorage.getAlert(alertId);
            if (alert == null) {
                return "未找到ID为 " + alertId + " 的告警";
            }
            
            String currentLevel = alert.getLevel();
            String newLevel;
            
            switch (currentLevel.toUpperCase()) {
                case "LOW":
                    newLevel = "MEDIUM";
                    break;
                case "MEDIUM":
                    newLevel = "HIGH";
                    break;
                case "HIGH":
                    newLevel = "CRITICAL";
                    break;
                case "CRITICAL":
                    return "告警已是最高级别（CRITICAL），无法再升级";
                default:
                    return "未知的告警级别：" + currentLevel;
            }
            
            alert.setLevel(newLevel);
            alert.setUpdateTime(LocalDateTime.now());
            
            return "告警级别已升级：" + alert.getName() + " 从 " + currentLevel + " 升级到 " + newLevel;
        } catch (Exception e) {
            log.error("升级告警失败", e);
            return "升级告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "批量更新告警状态")
    public String batchUpdateAlertStatus(
            @ToolParam(description = "告警ID列表，用逗号分隔") String alertIds,
            @ToolParam(description = "目标状态 (ACTIVE, RESOLVED, PENDING)") String targetStatus) {
        try {
            if (StrUtil.isBlank(alertIds) || StrUtil.isBlank(targetStatus)) {
                return "错误：告警ID列表和目标状态不能为空";
            }
            
            String[] validStatuses = {"ACTIVE", "RESOLVED", "PENDING"};
            if (!Arrays.asList(validStatuses).contains(targetStatus.toUpperCase())) {
                return "错误：无效的状态值，有效值为：" + String.join(", ", validStatuses);
            }
            
            String[] ids = alertIds.split(",");
            List<String> updatedAlerts = new ArrayList<>();
            List<String> notFoundAlerts = new ArrayList<>();
            
            for (String id : ids) {
                String trimmedId = id.trim();
                                 Alert alert = alertStorage.getAlert(trimmedId);
                if (alert != null) {
                    String oldStatus = alert.getStatus();
                    alert.setStatus(targetStatus.toUpperCase());
                    alert.setUpdateTime(LocalDateTime.now());
                    updatedAlerts.add(alert.getName() + " (ID: " + trimmedId + ") " + oldStatus + " → " + targetStatus);
                } else {
                    notFoundAlerts.add(trimmedId);
                }
            }
            
            StringBuilder result = new StringBuilder();
            result.append("批量状态更新结果：\n");
            result.append("成功更新 ").append(updatedAlerts.size()).append(" 条告警：\n");
            updatedAlerts.forEach(alert -> result.append("- ").append(alert).append("\n"));
            
            if (!notFoundAlerts.isEmpty()) {
                result.append("未找到的告警ID：\n");
                notFoundAlerts.forEach(id -> result.append("- ").append(id).append("\n"));
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("批量更新告警状态失败", e);
            return "批量更新告警状态失败：" + e.getMessage();
        }
    }
} 