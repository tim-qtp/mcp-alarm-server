package com.qtp.mcpserver.storage;

import com.qtp.mcpserver.entity.Alert;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警存储管理类 - 提供共享的告警数据存储
 */
public class AlertStorage {
    
    // 单例模式
    private static volatile AlertStorage instance;
    
    // 内存存储告警数据
    private final Map<String, Alert> alertStorage = new ConcurrentHashMap<>();
    
    private AlertStorage() {
        initSampleData();
    }
    
    public static AlertStorage getInstance() {
        if (instance == null) {
            synchronized (AlertStorage.class) {
                if (instance == null) {
                    instance = new AlertStorage();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化示例数据
     */
    private void initSampleData() {
        Alert alert1 = new Alert(UUID.randomUUID().toString(), "CPU告警", "系统资源", "HIGH", "ACTIVE", "CPU使用率过高", "监控系统", 
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), "cpu > 80%", "85%", "80%");
        Alert alert2 = new Alert(UUID.randomUUID().toString(), "内存告警", "系统资源", "MEDIUM", "PENDING", "内存使用率较高", "监控系统", 
                LocalDateTime.now().minusHours(1), LocalDateTime.now().minusMinutes(30), "memory > 70%", "75%", "70%");
        Alert alert3 = new Alert(UUID.randomUUID().toString(), "磁盘告警", "系统资源", "LOW", "RESOLVED", "磁盘空间不足", "监控系统", 
                LocalDateTime.now().minusHours(3), LocalDateTime.now().minusMinutes(10), "disk > 90%", "95%", "90%");
        
        alertStorage.put(alert1.getId(), alert1);
        alertStorage.put(alert2.getId(), alert2);
        alertStorage.put(alert3.getId(), alert3);
    }
    
    /**
     * 获取告警存储Map
     */
    public Map<String, Alert> getAlertStorage() {
        return alertStorage;
    }
    
    /**
     * 根据ID获取告警
     */
    public Alert getAlert(String id) {
        return alertStorage.get(id);
    }
    
    /**
     * 保存告警
     */
    public void saveAlert(Alert alert) {
        alertStorage.put(alert.getId(), alert);
    }
    
    /**
     * 删除告警
     */
    public Alert removeAlert(String id) {
        return alertStorage.remove(id);
    }
    
    /**
     * 获取所有告警
     */
    public Map<String, Alert> getAllAlerts() {
        return new ConcurrentHashMap<>(alertStorage);
    }
    
    /**
     * 清空所有告警（谨慎使用）
     */
    public void clearAll() {
        alertStorage.clear();
    }
    
    /**
     * 获取告警总数
     */
    public int size() {
        return alertStorage.size();
    }
} 