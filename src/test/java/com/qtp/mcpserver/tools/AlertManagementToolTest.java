package com.qtp.mcpserver.tools;

import com.qtp.mcpserver.storage.AlertStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AlertManagementTool的单元测试
 */
class AlertManagementToolTest {

    private AlertManagementTool alertManagementTool;
    private AlertTool alertTool;

    @BeforeEach
    void setUp() {
        alertManagementTool = new AlertManagementTool();
        alertTool = new AlertTool();
        // 清空存储，确保测试隔离
        AlertStorage.getInstance().clearAll();
    }

    @Test
    void testUpdateAlert() {
        // 先插入一个告警
        String insertResult = alertTool.insertAlert("测试告警", "测试类型", "LOW", "PENDING", 
            "原始描述", "原始来源", null, null, null);
        assertTrue(insertResult.contains("告警插入成功"));
        
        // 从存储中获取第一个告警的ID（简化处理）
        String alertId = AlertStorage.getInstance().getAllAlerts().keySet().iterator().next();
        
        // 更新告警
        String updateResult = alertManagementTool.updateAlert(alertId, "更新后的告警", 
            "更新类型", "HIGH", "ACTIVE", "更新描述", "更新来源", 
            "新规则", "新值", "新阈值");
        
        System.out.println("更新结果：" + updateResult);
        assertTrue(updateResult.contains("告警更新成功"));
        assertTrue(updateResult.contains("更新后的告警"));
        assertTrue(updateResult.contains("HIGH"));
    }

    @Test
    void testDeleteAlert() {
        // 先插入一个告警
        String insertResult = alertTool.insertAlert("待删除告警", "测试类型", "LOW", "PENDING", 
            "测试描述", "测试来源", null, null, null);
        assertTrue(insertResult.contains("告警插入成功"));
        
        String alertId = AlertStorage.getInstance().getAllAlerts().keySet().iterator().next();
        
        // 删除告警
        String deleteResult = alertManagementTool.deleteAlert(alertId);
        System.out.println("删除结果：" + deleteResult);
        assertTrue(deleteResult.contains("告警删除成功"));
        
        // 验证删除
        assertEquals(0, AlertStorage.getInstance().size());
    }

    @Test
    void testBatchDeleteAlerts() {
        // 插入多个告警
        alertTool.insertAlert("告警1", "类型1", "LOW", "PENDING", "描述1", "来源1", null, null, null);
        alertTool.insertAlert("告警2", "类型2", "MEDIUM", "ACTIVE", "描述2", "来源2", null, null, null);
        alertTool.insertAlert("告警3", "类型3", "HIGH", "RESOLVED", "描述3", "来源3", null, null, null);
        
        // 获取所有告警ID
        String[] alertIds = AlertStorage.getInstance().getAllAlerts().keySet().toArray(new String[0]);
        String alertIdsStr = String.join(",", alertIds[0], alertIds[1]);
        
        // 批量删除
        String batchDeleteResult = alertManagementTool.batchDeleteAlerts(alertIdsStr);
        System.out.println("批量删除结果：" + batchDeleteResult);
        assertTrue(batchDeleteResult.contains("成功删除 2 条告警"));
        
        // 验证还剩一个告警
        assertEquals(1, AlertStorage.getInstance().size());
    }

    @Test
    void testResolveAndActivateAlert() {
        // 插入告警
        alertTool.insertAlert("状态测试告警", "测试类型", "MEDIUM", "PENDING", 
            "测试描述", "测试来源", null, null, null);
        
        String alertId = AlertStorage.getInstance().getAllAlerts().keySet().iterator().next();
        
        // 解决告警
        String resolveResult = alertManagementTool.resolveAlert(alertId);
        System.out.println("解决告警结果：" + resolveResult);
        assertTrue(resolveResult.contains("告警已解决"));
        
        // 激活告警
        String activateResult = alertManagementTool.activateAlert(alertId);
        System.out.println("激活告警结果：" + activateResult);
        assertTrue(activateResult.contains("告警已激活"));
    }

    @Test
    void testGetAlertStatistics() {
        // 插入不同状态和级别的告警
        alertTool.insertAlert("告警1", "类型1", "LOW", "ACTIVE", "描述1", "来源1", null, null, null);
        alertTool.insertAlert("告警2", "类型1", "HIGH", "ACTIVE", "描述2", "来源2", null, null, null);
        alertTool.insertAlert("告警3", "类型2", "MEDIUM", "RESOLVED", "描述3", "来源3", null, null, null);
        alertTool.insertAlert("告警4", "类型2", "CRITICAL", "PENDING", "描述4", "来源4", null, null, null);
        
        String statsResult = alertManagementTool.getAlertStatistics();
        System.out.println("统计结果：" + statsResult);
        assertTrue(statsResult.contains("告警统计信息"));
        assertTrue(statsResult.contains("总告警数"));
        assertTrue(statsResult.contains("按状态统计"));
        assertTrue(statsResult.contains("按级别统计"));
        assertTrue(statsResult.contains("按类型统计"));
    }

    @Test
    void testSearchAlerts() {
        // 插入包含关键词的告警
        alertTool.insertAlert("CPU告警", "系统资源", "HIGH", "ACTIVE", "CPU使用率过高", "监控系统", null, null, null);
        alertTool.insertAlert("内存告警", "系统资源", "MEDIUM", "PENDING", "内存不足", "监控系统", null, null, null);
        alertTool.insertAlert("网络故障", "网络", "LOW", "RESOLVED", "网络连接超时", "网络监控", null, null, null);
        
        // 搜索包含"告警"的记录
        String searchResult = alertManagementTool.searchAlerts("告警", null, null);
        System.out.println("搜索结果：" + searchResult);
        assertTrue(searchResult.contains("搜索结果"));
        assertTrue(searchResult.contains("CPU告警"));
        assertTrue(searchResult.contains("内存告警"));
        
        // 搜索包含"网络"的记录
        String searchResult2 = alertManagementTool.searchAlerts("网络", null, null);
        System.out.println("搜索网络结果：" + searchResult2);
        assertTrue(searchResult2.contains("网络故障"));
    }

    @Test
    void testEscalateAlert() {
        // 插入低级别告警
        alertTool.insertAlert("低级别告警", "测试类型", "LOW", "ACTIVE", "测试描述", "测试来源", null, null, null);
        
        String alertId = AlertStorage.getInstance().getAllAlerts().keySet().iterator().next();
        
        // 升级告警级别
        String escalateResult = alertManagementTool.escalateAlert(alertId);
        System.out.println("升级结果：" + escalateResult);
        assertTrue(escalateResult.contains("告警级别已升级"));
        assertTrue(escalateResult.contains("从 LOW 升级到 MEDIUM"));
        
        // 再次升级
        String escalateResult2 = alertManagementTool.escalateAlert(alertId);
        System.out.println("再次升级结果：" + escalateResult2);
        assertTrue(escalateResult2.contains("从 MEDIUM 升级到 HIGH"));
        
        // 第三次升级
        String escalateResult3 = alertManagementTool.escalateAlert(alertId);
        System.out.println("第三次升级结果：" + escalateResult3);
        assertTrue(escalateResult3.contains("从 HIGH 升级到 CRITICAL"));
        
        // 第四次升级（应该失败）
        String escalateResult4 = alertManagementTool.escalateAlert(alertId);
        System.out.println("第四次升级结果：" + escalateResult4);
        assertTrue(escalateResult4.contains("已是最高级别"));
    }

    @Test
    void testBatchUpdateAlertStatus() {
        // 插入多个告警
        alertTool.insertAlert("告警1", "类型1", "LOW", "PENDING", "描述1", "来源1", null, null, null);
        alertTool.insertAlert("告警2", "类型2", "MEDIUM", "PENDING", "描述2", "来源2", null, null, null);
        alertTool.insertAlert("告警3", "类型3", "HIGH", "PENDING", "描述3", "来源3", null, null, null);
        
        // 获取所有告警ID
        String[] alertIds = AlertStorage.getInstance().getAllAlerts().keySet().toArray(new String[0]);
        String alertIdsStr = String.join(",", alertIds[0], alertIds[1]);
        
        // 批量更新状态
        String batchUpdateResult = alertManagementTool.batchUpdateAlertStatus(alertIdsStr, "ACTIVE");
        System.out.println("批量更新状态结果：" + batchUpdateResult);
        assertTrue(batchUpdateResult.contains("成功更新 2 条告警"));
        assertTrue(batchUpdateResult.contains("PENDING → ACTIVE"));
    }

    @Test
    void testGetAlertsByTimeRange() {
        // 由于时间范围查询需要具体的时间格式，这里测试时间格式验证
        String timeRangeResult = alertManagementTool.getAlertsByTimeRange(
            "2024-01-01 00:00:00", 
            "2024-12-31 23:59:59", 
            "CREATE"
        );
        System.out.println("时间范围查询结果：" + timeRangeResult);
        assertTrue(timeRangeResult.contains("时间范围查询结果"));
        
        // 测试无效时间格式
        String invalidTimeResult = alertManagementTool.getAlertsByTimeRange(
            "invalid-time", 
            "2024-12-31 23:59:59", 
            "CREATE"
        );
        System.out.println("无效时间格式结果：" + invalidTimeResult);
        assertTrue(invalidTimeResult.contains("时间格式不正确"));
    }

    @Test
    void testErrorHandling() {
        // 测试空ID错误处理
        String updateResult = alertManagementTool.updateAlert("", "名称", null, null, null, null, null, null, null, null);
        assertTrue(updateResult.contains("错误：告警ID不能为空"));
        
        String deleteResult = alertManagementTool.deleteAlert(null);
        assertTrue(deleteResult.contains("错误：告警ID不能为空"));
        
        String searchResult = alertManagementTool.searchAlerts("", null, null);
        assertTrue(searchResult.contains("错误：搜索关键词不能为空"));
        
        // 测试不存在的ID
        String nonExistentResult = alertManagementTool.updateAlert("non-existent-id", "名称", null, null, null, null, null, null, null, null);
        assertTrue(nonExistentResult.contains("未找到"));
    }
} 