package com.tigshop.bean.model.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信直播订单归因关系
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Data
@TableName("wechat_live_order_rel")
@Schema(description = "微信直播订单归因关系")
public class WechatLiveOrderRel implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单项ID")
    private Integer orderItemId;

    @Schema(description = "直播主键ID")
    private Integer wechatLiveId;

    @Schema(description = "直播房间ID")
    private Integer roomId;

    @Schema(description = "微信直播商品ID")
    private Integer wechatGoodsId;

    @Schema(description = "系统商品ID")
    private Integer productId;

    @Schema(description = "分享人OpenId")
    private String shareOpenId;

    @Schema(description = "观看人OpenId")
    private String viewerOpenId;

    @Schema(description = "直播场景值")
    private String scene;

    @Schema(description = "直播透传参数")
    private String customParams;

    @Schema(description = "渠道子来源")
    private String subSource;

    @Schema(description = "同步状态")
    private Integer syncStatus;

    @Schema(description = "最近同步时间")
    private Long lastSyncTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
