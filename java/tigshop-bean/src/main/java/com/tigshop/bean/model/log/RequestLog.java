package com.tigshop.bean.model.log;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author TigShop
 */
@Data
@TableName("request_log")
public class RequestLog {
    /**
     * 请求日志 ID
     */
    @TableId(value = "request_log_id", type = IdType.AUTO)
    private Integer requestLogId;
    /**
     * 请求的 URL，例如 /api/user/update
     */
    private String url;

    /**
     * HTTP 方法，例如 POST
     */
    private String httpMethod;

    /**
     * 类方法，例如 UserController.updateUser(..)
     */
    private String classMethod;

    /**
     * 请求参数（JSON 格式存储）
     */
    private String requestParams;

    /**
     * 操作时间
     */
    private String addTime;

    /**
     * 后台用户id
     */
    private Integer adminId;

    /**
     * 后台用户ip
     */
    private String adminIp;

    /**
     * 平台
     */
    private String platform;

    /**
     * 后台用户名
     */
    private String adminUsername;
}
