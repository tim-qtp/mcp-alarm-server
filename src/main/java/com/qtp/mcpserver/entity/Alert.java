package com.qtp.mcpserver.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Date;

/**
 * Alert实体类，对应MongoDB的alert集合，字段与数据库表结构一一对应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "alert")
public class Alert {
    @Id
    private String id; // _id
//    @Field("case_exec_id")
    private String caseExecId; // case_exec_id
//    @Field("alarm_level")
    private String alarmLevel; // alarm_level
//    @Field("alarm_type")
    private String alarmType; // alarm_type
//    @Field("alert_id")
    private String alertId; // alert_id
//    @Field("company")
    private String company; // company
//    @Field("end_time")
    private Date endTime; // end_time
//    @Field("fail_reason")
    private String failReason; // fail_reason
//    @Field("is_recover")
    private Boolean isRecover; // is_recover
//    @Field("layer_name")
    private String layerName; // layer_name
//    @Field("region_name")
    private String regionName; // region_name
//    @Field("status")
    private Integer status; // status
//    @Field("system_name")
    private String systemName; // system_name
//    @Field("task_name")
    private String taskName; // task_name
//    @Field("type")
    private Integer type; // type
//    @Field("recover_time")
    private Date recoverTime; // recover_time
//    @Field("ave_time")
    private String aveTime; // ave_time
//    @Field("begin_time")
    private Date beginTime; // begin_time
//    @Field("host")
    private String host; // host
//    @Field("response")
    private String response; // response
//    @Field("actual_value")
    private String actualValue; // actual_value
//    @Field("is_reply")
    private Integer isReply; // is_reply

}