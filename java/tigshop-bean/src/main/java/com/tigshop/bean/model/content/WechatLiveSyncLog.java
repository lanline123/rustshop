package com.tigshop.bean.model.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信直播同步日志
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Data
@TableName("wechat_live_sync_log")
@Schema(description = "微信直播同步日志")
public class WechatLiveSyncLog implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "退款ID")
    private Integer refundId;

    @Schema(description = "直播房间ID")
    private Integer roomId;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "同步方向")
    private String direction;

    @Schema(description = "同步状态")
    private Integer syncStatus;

    @Schema(description = "幂等键")
    private String idempotentKey;

    @Schema(description = "请求数据")
    private String requestData;

    @Schema(description = "响应数据")
    private String responseData;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "最近同步时间")
    private Long lastSyncTime;

    @Schema(description = "下次重试时间")
    private Long nextRetryTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
