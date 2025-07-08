package com.qtp.mcpserver.repository;

import com.qtp.mcpserver.entity.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {
    // 可扩展自定义查询方法
} 