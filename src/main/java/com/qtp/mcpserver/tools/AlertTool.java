package com.qtp.mcpserver.tools;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qtp.mcpserver.entity.Alert;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

/**
 * 基于MongoDB的告警MCP工具，所有数据均来源于数据库。
 */
@Service
@Slf4j
public class AlertTool {
    
    private final MongoTemplate mongoTemplate;
    
    // 自定义的JSON序列化器
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AlertTool(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * 自定义JSON序列化方法
     */
    private String toJsonString(Object obj) {
        try {
            if (obj instanceof AlertPageResult) {
                AlertPageResult page = (AlertPageResult) obj;
                return String.format("第%d页, 每页%d, 总数%d\n%s", page.getPageNum(), page.getPageSize(), page.getTotal(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(page.getData()));
            }
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return obj.toString();
        }
    }

    @Tool(description = "查询某个告警的详细信息")
    public String queryAlert(@ToolParam(description = "告警ID") String alertId) {
        try {
            if (StrUtil.isBlank(alertId)) {
                return "错误：告警ID不能为空";
            }
            Alert alert = mongoTemplate.findById(alertId, Alert.class);
            if (alert == null) {
                return "未找到ID为 " + alertId + " 的告警";
            }
            return toJsonString(alert);
        } catch (Exception e) {
            log.error("查询告警失败", e);
            return "查询告警失败：" + e.getMessage();
        }
    }

    @Tool(description = "查询告警信息列表，支持按状态、级别、类型筛选")
    public String queryAlertList(
            @ToolParam(description = "告警状态筛选（Int32），可选") Integer status,
            @ToolParam(description = "告警等级筛选（String），可选") String alarmLevel,
            @ToolParam(description = "告警类型筛选（String），可选") String alarmType,
            @ToolParam(description = "每页数量，默认10") Integer pageSize,
            @ToolParam(description = "页码，默认1") Integer pageNum,
            @ToolParam(description = "排序字段（如endTime），可选") String sortField,
            @ToolParam(description = "排序方式，asc/desc，可选") String sortOrder) {
        try {
            if (pageSize == null || pageSize <= 0) pageSize = 10;
            if (pageNum == null || pageNum <= 0) pageNum = 1;
            if (sortField == null) sortField = "endTime";
            Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Query query = new Query();
            if (status != null) query.addCriteria(Criteria.where("status").is(status));
            if (alarmLevel != null) query.addCriteria(Criteria.where("alarmLevel").is(alarmLevel));
            if (alarmType != null) query.addCriteria(Criteria.where("alarmType").is(alarmType));
            query.with(Sort.by(direction, sortField));
            long total = mongoTemplate.count(query, Alert.class);
            query.skip((long) (pageNum - 1) * pageSize).limit(pageSize);
            java.util.List<Alert> alerts = mongoTemplate.find(query, Alert.class);
            return toJsonString(new AlertPageResult(alerts, total, pageNum, pageSize));
        } catch (Exception e) {
            log.error("查询告警列表失败", e);
            return "查询告警列表失败：" + e.getMessage();
        }
    }
    
    @Tool(description = "多条件分页查询告警信息列表")
    public String searchAlerts(
            @ToolParam(description = "告警等级（String），可选") String alarmLevel,
            @ToolParam(description = "告警类型（String），可选") String alarmType,
            @ToolParam(description = "所属单位（String），可选") String company,
            @ToolParam(description = "告警状态（Int32），可选") Integer status,
            @ToolParam(description = "开始时间（Date），可选") Date beginTime,
            @ToolParam(description = "结束时间（Date），可选") Date endTime,
            @ToolParam(description = "页码，默认1") Integer pageNum,
            @ToolParam(description = "每页数量，默认10") Integer pageSize,
            @ToolParam(description = "排序字段（如endTime），可选") String sortField,
            @ToolParam(description = "排序方式，asc/desc，可选") String sortOrder
    ) {
        try {
            if (pageNum == null || pageNum <= 0) pageNum = 1;
            if (pageSize == null || pageSize <= 0) pageSize = 10;
            if (sortField == null) sortField = "endTime";
            Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Query query = new Query();
            if (alarmLevel != null) query.addCriteria(Criteria.where("alarmLevel").is(alarmLevel));
            if (alarmType != null) query.addCriteria(Criteria.where("alarmType").is(alarmType));
            if (company != null) query.addCriteria(Criteria.where("company").is(company));
            if (status != null) query.addCriteria(Criteria.where("status").is(status));
            if (beginTime != null && endTime != null) {
                query.addCriteria(Criteria.where("endTime").gte(beginTime).lte(endTime));
            } else if (beginTime != null) {
                query.addCriteria(Criteria.where("endTime").gte(beginTime));
            } else if (endTime != null) {
                query.addCriteria(Criteria.where("endTime").lte(endTime));
            }
            query.with(Sort.by(direction, sortField));
            long total = mongoTemplate.count(query, Alert.class);
            query.skip((long) (pageNum - 1) * pageSize).limit(pageSize);
            java.util.List<Alert> alerts = mongoTemplate.find(query, Alert.class);
            return toJsonString(new AlertPageResult(alerts, total, pageNum, pageSize));
        } catch (Exception e) {
            log.error("多条件分页查询告警失败", e);
            return "多条件分页查询告警失败：" + e.getMessage();
        }
    }

    @Data
    @AllArgsConstructor
    static class AlertPageResult {
        private List<Alert> data;
        private long total;
        private int pageNum;
        private int pageSize;
    }
} 