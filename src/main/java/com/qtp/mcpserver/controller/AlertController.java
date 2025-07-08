package com.qtp.mcpserver.controller;

import com.qtp.mcpserver.entity.Alert;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 多条件分页查询告警
     */
    @GetMapping("/search")
    public AlertPageResult searchAlerts(
            @RequestParam(required = false) String caseExecId,
            @RequestParam(required = false) String alarmLevel,
            @RequestParam(required = false) String alarmType,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String systemName,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "endTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Query query = new Query();
        
        // 添加查询条件
        if (caseExecId != null && !caseExecId.trim().isEmpty()) {
            query.addCriteria(Criteria.where("caseExecId").regex(caseExecId, "i"));
        }
        if (alarmLevel != null && !alarmLevel.trim().isEmpty()) {
            query.addCriteria(Criteria.where("alarmLevel").is(alarmLevel));
        }
        if (alarmType != null && !alarmType.trim().isEmpty()) {
            query.addCriteria(Criteria.where("alarmType").is(alarmType));
        }
        if (company != null && !company.trim().isEmpty()) {
            query.addCriteria(Criteria.where("company").regex(company, "i"));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        if (systemName != null && !systemName.trim().isEmpty()) {
            query.addCriteria(Criteria.where("systemName").regex(systemName, "i"));
        }
        if (taskName != null && !taskName.trim().isEmpty()) {
            query.addCriteria(Criteria.where("taskName").regex(taskName, "i"));
        }
        if (host != null && !host.trim().isEmpty()) {
            query.addCriteria(Criteria.where("host").regex(host, "i"));
        }
        if (beginTime != null && endTime != null) {
            query.addCriteria(Criteria.where("endTime").gte(beginTime).lte(endTime));
        } else if (beginTime != null) {
            query.addCriteria(Criteria.where("endTime").gte(beginTime));
        } else if (endTime != null) {
            query.addCriteria(Criteria.where("endTime").lte(endTime));
        }

        // 排序
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        query.with(Sort.by(direction, sortField));
        
        // 分页
        long total = mongoTemplate.count(query, Alert.class);
        query.skip((long) (pageNum - 1) * pageSize).limit(pageSize);
        List<Alert> alerts = mongoTemplate.find(query, Alert.class);
        
        return new AlertPageResult(alerts, total, pageNum, pageSize);
    }

    /**
     * 根据ID获取告警详情
     */
    @GetMapping("/{id}")
    public Alert getAlertById(@PathVariable String id) {
        return mongoTemplate.findById(id, Alert.class);
    }

    /**
     * 创建新告警
     */
    @PostMapping
    public Alert createAlert(@RequestBody Alert alert) {
        return mongoTemplate.save(alert);
    }

    /**
     * 更新告警
     */
    @PutMapping("/{id}")
    public Alert updateAlert(@PathVariable String id, @RequestBody Alert alert) {
        Alert existingAlert = mongoTemplate.findById(id, Alert.class);
        if (existingAlert == null) {
            throw new RuntimeException("告警不存在");
        }
        alert.setId(id);
        return mongoTemplate.save(alert);
    }

    /**
     * 删除告警
     */
    @DeleteMapping("/{id}")
    public String deleteAlert(@PathVariable String id) {
        Alert alert = mongoTemplate.findById(id, Alert.class);
        if (alert == null) {
            return "告警不存在";
        }
        mongoTemplate.remove(alert);
        return "删除成功";
    }

    /**
     * 批量删除告警
     */
    @DeleteMapping("/batch")
    public Map<String, Object> batchDeleteAlerts(@RequestBody List<String> ids) {
        Map<String, Object> result = new HashMap<>();
        List<String> deletedIds = new java.util.ArrayList<>();
        List<String> notFoundIds = new java.util.ArrayList<>();
        
        for (String id : ids) {
            Alert alert = mongoTemplate.findById(id, Alert.class);
            if (alert != null) {
                mongoTemplate.remove(alert);
                deletedIds.add(id);
            } else {
                notFoundIds.add(id);
            }
        }
        
        result.put("deletedCount", deletedIds.size());
        result.put("deletedIds", deletedIds);
        result.put("notFoundIds", notFoundIds);
        return result;
    }

    /**
     * 解决告警
     */
    @PutMapping("/{id}/resolve")
    public Alert resolveAlert(@PathVariable String id) {
        Alert alert = mongoTemplate.findById(id, Alert.class);
        if (alert == null) {
            throw new RuntimeException("告警不存在");
        }
        alert.setStatus(1);
        alert.setIsRecover(true);
        alert.setRecoverTime(new Date());
        return mongoTemplate.save(alert);
    }

    /**
     * 激活告警
     */
    @PutMapping("/{id}/activate")
    public Alert activateAlert(@PathVariable String id) {
        Alert alert = mongoTemplate.findById(id, Alert.class);
        if (alert == null) {
            throw new RuntimeException("告警不存在");
        }
        alert.setStatus(0);
        alert.setIsRecover(false);
        return mongoTemplate.save(alert);
    }

    /**
     * 升级告警级别
     */
    @PutMapping("/{id}/escalate")
    public Alert escalateAlert(@PathVariable String id) {
        Alert alert = mongoTemplate.findById(id, Alert.class);
        if (alert == null) {
            throw new RuntimeException("告警不存在");
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
                throw new RuntimeException("告警已是最高级别");
            default:
                throw new RuntimeException("未知的告警级别：" + currentLevel);
        }
        
        alert.setAlarmLevel(newLevel);
        return mongoTemplate.save(alert);
    }

    /**
     * 批量更新告警状态
     */
    @PutMapping("/batch/status")
    public Map<String, Object> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) request.get("ids");
        Integer targetStatus = (Integer) request.get("status");
        
        Map<String, Object> result = new HashMap<>();
        List<String> updatedIds = new java.util.ArrayList<>();
        List<String> notFoundIds = new java.util.ArrayList<>();
        
        for (String id : ids) {
            Alert alert = mongoTemplate.findById(id, Alert.class);
            if (alert != null) {
                alert.setStatus(targetStatus);
                mongoTemplate.save(alert);
                updatedIds.add(id);
            } else {
                notFoundIds.add(id);
            }
        }
        
        result.put("updatedCount", updatedIds.size());
        result.put("updatedIds", updatedIds);
        result.put("notFoundIds", notFoundIds);
        return result;
    }

    /**
     * 获取告警统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        List<Alert> alerts = mongoTemplate.findAll(Alert.class);
        
        // 按状态统计
        Map<Integer, Long> statusStats = alerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(Alert::getStatus, java.util.stream.Collectors.counting()));
        
        // 按级别统计
        Map<String, Long> levelStats = alerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(Alert::getAlarmLevel, java.util.stream.Collectors.counting()));
        
        // 按类型统计
        Map<String, Long> typeStats = alerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(Alert::getAlarmType, java.util.stream.Collectors.counting()));
        
        // 按公司统计
        Map<String, Long> companyStats = alerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(Alert::getCompany, java.util.stream.Collectors.counting()));
        
        // 按系统统计
        Map<String, Long> systemStats = alerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(Alert::getSystemName, java.util.stream.Collectors.counting()));
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", alerts.size());
        statistics.put("statusStats", statusStats);
        statistics.put("levelStats", levelStats);
        statistics.put("typeStats", typeStats);
        statistics.put("companyStats", companyStats);
        statistics.put("systemStats", systemStats);
        
        return statistics;
    }

    /**
     * 全文搜索告警
     */
    @GetMapping("/search/fulltext")
    public AlertPageResult fullTextSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
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
        
        return new AlertPageResult(alerts, total, pageNum, pageSize);
    }

    /**
     * 按时间范围查询告警
     */
    @GetMapping("/search/timeRange")
    public List<Alert> searchByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime
    ) {
        Query query = new Query();
        query.addCriteria(Criteria.where("endTime").gte(startTime).lte(endTime));
        return mongoTemplate.find(query, Alert.class);
    }

    /**
     * 获取告警级别选项
     */
    @GetMapping("/options/levels")
    public List<String> getAlarmLevels() {
        return List.of("info", "warning", "critical");
    }

    /**
     * 获取告警类型选项
     */
    @GetMapping("/options/types")
    public List<String> getAlarmTypes() {
        return List.of("host", "business");
    }

    /**
     * 获取状态选项
     */
    @GetMapping("/options/statuses")
    public Map<String, String> getStatusOptions() {
        Map<String, String> statuses = new HashMap<>();
        statuses.put("0", "活跃");
        statuses.put("1", "已解决");
        statuses.put("2", "待处理");
        return statuses;
    }

    @Data
    @AllArgsConstructor
    public static class AlertPageResult {
        private List<Alert> data;
        private long total;
        private int pageNum;
        private int pageSize;
    }
} 