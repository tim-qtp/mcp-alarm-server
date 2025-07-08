package com.qtp.mcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qtp.mcpserver.entity.Alert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AlertManagementTool {
    
    // 自定义的JSON序列化器
    private final ObjectMapper objectMapper;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public AlertManagementTool() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * 自定义JSON序列化方法
     */
    private String toJsonString(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return JSONUtil.toJsonPrettyStr(obj);
        }
    }
    
    @Tool(description = "更新告警信息")
    public String updateAlert(
            @ToolParam(description = "文档ID") String id,
            @ToolParam(description = "告警ID") String caseExecId,
            @ToolParam(description = "告警等级") String alarmLevel,
            @ToolParam(description = "告警类型") String alarmType,
            @ToolParam(description = "告警唯一标识符") String alertId,
            @ToolParam(description = "所属单位") String company,
            @ToolParam(description = "告警时间") Date endTime,
            @ToolParam(description = "告警信息") String failReason,
            @ToolParam(description = "是否恢复") Boolean isRecover,
            @ToolParam(description = "告警来源") String layerName,
            @ToolParam(description = "所属网域") String regionName,
            @ToolParam(description = "告警状态") Integer status,
            @ToolParam(description = "所属系统") String systemName,
            @ToolParam(description = "所属模块") String taskName,
            @ToolParam(description = "类型") Integer type,
            @ToolParam(description = "恢复时间") Date recoverTime,
            @ToolParam(description = "平均持续时长") String aveTime,
            @ToolParam(description = "探测开始时间") Date beginTime,
            @ToolParam(description = "ip") String host,
            @ToolParam(description = "接口探测报文") String response,
            @ToolParam(description = "ui探测报文") String actualValue,
            @ToolParam(description = "是否响应") Integer isReply
    ) {
        try {
            if (StrUtil.isBlank(id)) {
                return "错误：文档ID不能为空";
            }
            Alert alert = mongoTemplate.findById(id, Alert.class);
            if (alert == null) {
                return "未找到ID为 " + id + " 的告警";
            }
            if (StrUtil.isNotBlank(caseExecId)) alert.setCaseExecId(caseExecId);
            if (StrUtil.isNotBlank(alarmLevel)) alert.setAlarmLevel(alarmLevel);
            if (StrUtil.isNotBlank(alarmType)) alert.setAlarmType(alarmType);
            if (StrUtil.isNotBlank(alertId)) alert.setAlertId(alertId);
            if (StrUtil.isNotBlank(company)) alert.setCompany(company);
            if (endTime != null) alert.setEndTime(endTime);
            if (StrUtil.isNotBlank(failReason)) alert.setFailReason(failReason);
            if (isRecover != null) alert.setIsRecover(isRecover);
            if (StrUtil.isNotBlank(layerName)) alert.setLayerName(layerName);
            if (StrUtil.isNotBlank(regionName)) alert.setRegionName(regionName);
            if (status != null) alert.setStatus(status);
            if (StrUtil.isNotBlank(systemName)) alert.setSystemName(systemName);
            if (StrUtil.isNotBlank(taskName)) alert.setTaskName(taskName);
            if (type != null) alert.setType(type);
            if (recoverTime != null) alert.setRecoverTime(recoverTime);
            if (StrUtil.isNotBlank(aveTime)) alert.setAveTime(aveTime);
            if (beginTime != null) alert.setBeginTime(beginTime);
            if (StrUtil.isNotBlank(host)) alert.setHost(host);
            if (StrUtil.isNotBlank(response)) alert.setResponse(response);
            if (StrUtil.isNotBlank(actualValue)) alert.setActualValue(actualValue);
            if (isReply != null) alert.setIsReply(isReply);
            mongoTemplate.save(alert);
            return toJsonString(alert);
        } catch (Exception e) {
            log.error("更新告警失败", e);
            return "更新告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "删除告警")
    public String deleteAlert(@ToolParam(description = "文档ID") String id) {
        try {
            if (StrUtil.isBlank(id)) {
                return "错误：文档ID不能为空";
            }
            Alert alert = mongoTemplate.findById(id, Alert.class);
            if (alert == null) {
                return "未找到ID为 " + id + " 的告警";
            }
            mongoTemplate.remove(alert);
            return "告警删除成功：" + alert.getCaseExecId() + " (ID: " + id + ")";
        } catch (Exception e) {
            log.error("删除告警失败", e);
            return "删除告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "批量删除告警")
    public String batchDeleteAlerts(@ToolParam(description = "文档ID列表，用逗号分隔") String ids) {
        try {
            if (StrUtil.isBlank(ids)) {
                return "错误：文档ID列表不能为空";
            }
            String[] idArray = ids.split(",");
            List<String> deletedAlerts = new ArrayList<>();
            List<String> notFoundAlerts = new ArrayList<>();
            for (String id : idArray) {
                String trimmedId = id.trim();
                Alert alert = mongoTemplate.findById(trimmedId, Alert.class);
                if (alert != null) {
                    mongoTemplate.remove(alert);
                    deletedAlerts.add(alert.getCaseExecId() + " (ID: " + trimmedId + ")");
                } else {
                    notFoundAlerts.add(trimmedId);
                }
            }
            StringBuilder result = new StringBuilder();
            result.append("批量删除结果：\n");
            result.append("成功删除 ").append(deletedAlerts.size()).append(" 条告警：\n");
            deletedAlerts.forEach(a -> result.append("- ").append(a).append("\n"));
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
    
    @Tool(description = "解决告警（将状态设置为已解决）")
    public String resolveAlert(@ToolParam(description = "文档ID") String id) {
        try {
            if (StrUtil.isBlank(id)) {
                return "错误：文档ID不能为空";
            }
            Alert alert = mongoTemplate.findById(id, Alert.class);
            if (alert == null) {
                return "未找到ID为 " + id + " 的告警";
            }
            alert.setStatus(1); // 假设1表示已解决
            alert.setIsRecover(true);
            alert.setRecoverTime(new Date());
            mongoTemplate.save(alert);
            return "告警已解决：" + alert.getCaseExecId() + " (ID: " + id + ")";
        } catch (Exception e) {
            log.error("解决告警失败", e);
            return "解决告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "激活告警（将状态设置为活跃）")
    public String activateAlert(@ToolParam(description = "文档ID") String id) {
        try {
            if (StrUtil.isBlank(id)) {
                return "错误：文档ID不能为空";
            }
            Alert alert = mongoTemplate.findById(id, Alert.class);
            if (alert == null) {
                return "未找到ID为 " + id + " 的告警";
            }
            alert.setStatus(0); // 假设0表示活跃
            alert.setIsRecover(false);
            mongoTemplate.save(alert);
            return "告警已激活：" + alert.getCaseExecId() + " (ID: " + id + ")";
        } catch (Exception e) {
            log.error("激活告警失败", e);
            return "激活告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "获取告警统计信息")
    public String getAlertStatistics() {
        try {
            List<Alert> alerts = mongoTemplate.findAll(Alert.class);
            
            // 按状态统计
            Map<Integer, Long> statusStats = new HashMap<>();
            statusStats = alerts.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Alert::getStatus, java.util.stream.Collectors.counting()));
            
            // 按级别统计
            Map<String, Long> levelStats = new HashMap<>();
            levelStats = alerts.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Alert::getAlarmLevel, java.util.stream.Collectors.counting()));
            
            // 按类型统计
            Map<String, Long> typeStats = new HashMap<>();
            typeStats = alerts.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Alert::getAlarmType, java.util.stream.Collectors.counting()));
            
            // 按公司统计
            Map<String, Long> companyStats = new HashMap<>();
            companyStats = alerts.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Alert::getCompany, java.util.stream.Collectors.counting()));
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("总告警数", alerts.size());
            statistics.put("按状态统计", statusStats);
            statistics.put("按级别统计", levelStats);
            statistics.put("按类型统计", typeStats);
            statistics.put("按公司统计", companyStats);
            
            return "告警统计信息：\n" + toJsonString(statistics);
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
            
            Query query = new Query();
            query.addCriteria(new Criteria().orOperator(
                Criteria.where("caseExecId").regex(keyword, "i"),
                Criteria.where("failReason").regex(keyword, "i"),
                Criteria.where("alarmType").regex(keyword, "i"),
                Criteria.where("layerName").regex(keyword, "i"),
                Criteria.where("company").regex(keyword, "i"),
                Criteria.where("systemName").regex(keyword, "i"),
                Criteria.where("taskName").regex(keyword, "i"),
                Criteria.where("host").regex(keyword, "i")
            ));
            
            long total = mongoTemplate.count(query, Alert.class);
            query.skip((long) (pageNum - 1) * pageSize).limit(pageSize);
            List<Alert> alerts = mongoTemplate.find(query, Alert.class);
            
            return "搜索结果（关键词：" + keyword + "，第" + pageNum + "页，共" + total + "条）：\n" + 
                   toJsonString(alerts);
        } catch (Exception e) {
            log.error("搜索告警失败", e);
            return "搜索告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "按时间范围查询告警")
    public String getAlertsByTimeRange(
            @ToolParam(description = "开始时间") Date startTime,
            @ToolParam(description = "结束时间") Date endTime) {
        try {
            if (startTime == null || endTime == null) {
                return "错误：开始时间和结束时间不能为空";
            }
            
            if (startTime.after(endTime)) {
                return "错误：开始时间不能晚于结束时间";
            }
            
            Query query = new Query();
            query.addCriteria(Criteria.where("endTime").gte(startTime).lte(endTime));
            
            List<Alert> alerts = mongoTemplate.find(query, Alert.class);
            
            return "时间范围查询结果（" + startTime + " 至 " + endTime + "，共" + alerts.size() + "条）：\n" + 
                   toJsonString(alerts);
        } catch (Exception e) {
            log.error("按时间范围查询告警失败", e);
            return "按时间范围查询告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "升级告警级别")
    public String escalateAlert(@ToolParam(description = "文档ID") String id) {
        try {
            if (StrUtil.isBlank(id)) {
                return "错误：文档ID不能为空";
            }
            
            Alert alert = mongoTemplate.findById(id, Alert.class);
            if (alert == null) {
                return "未找到ID为 " + id + " 的告警";
            }
            
            String currentLevel = alert.getAlarmLevel();
            String newLevel;
            
            switch (currentLevel.toLowerCase()) {
                case "info":
                    newLevel = "warning";
                    break;
                case "warning":
                    newLevel = "critical";
                    break;
                case "critical":
                    return "告警已是最高级别（critical），无法再升级";
                default:
                    return "未知的告警级别：" + currentLevel;
            }
            
            alert.setAlarmLevel(newLevel);
            mongoTemplate.save(alert);
            
            return "告警级别已升级：" + alert.getCaseExecId() + " 从 " + currentLevel + " 升级到 " + newLevel;
        } catch (Exception e) {
            log.error("升级告警失败", e);
            return "升级告警失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "批量更新告警状态")
    public String batchUpdateAlertStatus(
            @ToolParam(description = "文档ID列表，用逗号分隔") String ids,
            @ToolParam(description = "目标状态") Integer targetStatus) {
        try {
            if (StrUtil.isBlank(ids) || targetStatus == null) {
                return "错误：文档ID列表和目标状态不能为空";
            }
            
            String[] idArray = ids.split(",");
            List<String> updatedAlerts = new ArrayList<>();
            List<String> notFoundAlerts = new ArrayList<>();
            
            for (String id : idArray) {
                String trimmedId = id.trim();
                Alert alert = mongoTemplate.findById(trimmedId, Alert.class);
                if (alert != null) {
                    Integer oldStatus = alert.getStatus();
                    alert.setStatus(targetStatus);
                    updatedAlerts.add(alert.getCaseExecId() + " (ID: " + trimmedId + ") " + oldStatus + " → " + targetStatus);
                    mongoTemplate.save(alert);
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