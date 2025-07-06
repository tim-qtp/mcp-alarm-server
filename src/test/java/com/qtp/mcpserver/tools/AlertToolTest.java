package com.qtp.mcpserver.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertToolTest {

    private final AlertTool alertTool = new AlertTool();

    @Test
    void testQueryAlert() {
        // 先插入一个告警获取ID
        String insertResult = alertTool.insertAlert("测试告警", "测试类型", "MEDIUM", "ACTIVE", "测试描述", "测试系统", null, null, null);
        System.out.println("插入告警结果：" + insertResult);
        
        // 从插入结果中提取ID (简化处理，实际应用中可以有更好的方式)
        String testId = "test-uuid-123"; // 由于无法直接获取，使用一个已知不存在的ID来测试
        
        // 测试查询不存在的告警
        String result2 = alertTool.queryAlert(testId);
        System.out.println("查询不存在告警结果：" + result2);
        assertTrue(result2.contains("未找到"));
        
        // 测试空ID
        String result3 = alertTool.queryAlert(null);
        System.out.println("查询空ID结果：" + result3);
        assertTrue(result3.contains("错误"));
        
        // 测试空字符串ID
        String result4 = alertTool.queryAlert("");
        System.out.println("查询空字符串ID结果：" + result4);
        assertTrue(result4.contains("错误"));
    }

    @Test
    void testQueryAlertList() {
        // 测试查询所有告警
        String result = alertTool.queryAlertList(null, null, null, null, null);
        System.out.println("查询所有告警结果：" + result);
        assertTrue(result.contains("告警列表"));
        
        // 测试按状态筛选
        String result2 = alertTool.queryAlertList("ACTIVE", null, null, null, null);
        System.out.println("查询ACTIVE告警结果：" + result2);
        assertTrue(result2.contains("告警列表"));
        
        // 测试按级别筛选
        String result3 = alertTool.queryAlertList(null, "HIGH", null, null, null);
        System.out.println("查询HIGH级别告警结果：" + result3);
        assertTrue(result3.contains("告警列表"));
        
        // 测试分页
        String result4 = alertTool.queryAlertList(null, null, null, 1, 2);
        System.out.println("查询分页告警结果：" + result4);
        assertTrue(result4.contains("第2页"));
    }

    @Test
    void testInsertAlert() {
        // 测试插入新告警
        String result = alertTool.insertAlert(
            "测试告警", 
            "测试类型", 
            "MEDIUM", 
            "ACTIVE", 
            "这是一个测试告警", 
            "测试系统", 
            "test > 50%", 
            "60%", 
            "50%"
        );
        System.out.println("插入告警结果：" + result);
        assertTrue(result.contains("告警插入成功"));
        assertTrue(result.contains("测试告警"));
        
        // 测试必填字段校验
        String result2 = alertTool.insertAlert(null, null, null, null, null, null, null, null, null);
        System.out.println("插入空告警结果：" + result2);
        assertTrue(result2.contains("错误"));
    }

    @Test
    void testBatchInsertAlerts() {
        // 测试批量插入告警
        String alertsJson = "[{\"name\":\"批量告警1\",\"type\":\"批量类型\",\"level\":\"LOW\",\"status\":\"PENDING\",\"description\":\"批量测试1\"}," +
                           "{\"name\":\"批量告警2\",\"type\":\"批量类型\",\"level\":\"MEDIUM\",\"status\":\"ACTIVE\",\"description\":\"批量测试2\"}]";
        
        String result = alertTool.batchInsertAlerts(alertsJson);
        System.out.println("批量插入告警结果：" + result);
        assertTrue(result.contains("批量插入成功"));
        assertTrue(result.contains("共插入 2 条告警"));
        
        // 测试空JSON
        String result2 = alertTool.batchInsertAlerts(null);
        System.out.println("批量插入空JSON结果：" + result2);
        assertTrue(result2.contains("错误"));
        
        // 测试无效JSON
        String result3 = alertTool.batchInsertAlerts("invalid json");
        System.out.println("批量插入无效JSON结果：" + result3);
        assertTrue(result3.contains("失败"));
    }

    @Test
    void testIntegration() {
        // 集成测试：插入告警后查询
        String insertResult = alertTool.insertAlert(
            "集成测试告警", 
            "集成测试", 
            "CRITICAL", 
            "ACTIVE", 
            "集成测试描述", 
            "集成测试系统", 
            "integration > 100%", 
            "120%", 
            "100%"
        );
        System.out.println("集成测试插入结果：" + insertResult);
        assertTrue(insertResult.contains("告警插入成功"));
        
        // 查询刚插入的告警
        String queryResult = alertTool.queryAlertList("ACTIVE", "CRITICAL", "集成测试", null, null);
        System.out.println("集成测试查询结果：" + queryResult);
        assertTrue(queryResult.contains("集成测试告警"));
    }
} 