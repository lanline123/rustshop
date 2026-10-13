package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 获取直播列表和回放返回
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "获取直播列表和回放返回")
public class GetLiveInfoResult {
    @Schema(description = "返回码")
    private Long errcode;

    @Schema(description = "错误信息")
    private String errmsg;

    @Schema(description = "拉取房间总数")
    private Long total;

    @Schema(description = "房间信息列表")
    private List<RoomInfo> roomInfoList;

    @Schema(description = "action=\"get_replay\"才返回")
    private List<ReplayInfo> liveReplay;

    @Data
    @Schema(description = "商品")
    public static class ProductInfo {
        @Schema(description = "商品名称")
        private String name;

        @Schema(description = "商品封面图链接")
        private String coverImg;

        @Schema(description = "商品小程序路径")
        private String url;

        @Schema(description = "商品价格（分）")
        private BigDecimal price;

        @Schema(description = "商品价格，使用方式看price_type")
        private Integer priceType;

        @Schema(description = "商品id")
        private Integer goodsId;

        @Schema(description = "第三方商品appid ,当前小程序商品则为空")
        private String thirdPartyAppid;
    }

    @Data
    @Schema(description = "action=\"get_replay\"才返回")
    public static class ReplayInfo {
        @Schema(description = "回放视频创建时间")
        private String 	createTime;

        @Schema(description = "回放视频url过期时间")
        private String 	expireTime;

        @Schema(description = "回放视频链接")
        private Long mediaUrl;
    }

    @Data
    @Schema(description = "房间信息")
    public static class RoomInfo {
        @Schema(description = "房间id")
        private Integer roomId;

        @Schema(description = "房间名称")
        private String name;

        @Schema(description = "直播间背景图链接")
        private String coverImg;

        @Schema(description = "直播间开始时间，列表按照start_time降序排列")
        private Long startTime;

        @Schema(description = "直播计划结束时间")
        private Long endTime;

        @Schema(description = "主播名")
        private String anchorName;

        @Schema(description = "直播间状态。101：直播中，102：未开始，103已结束，104禁播，105：暂停，106：异常，107：已过期")
        private Integer status;

        @Schema(description = "直播间分享图链接")
        private String shareImg;

        @Schema(description = "直播类型，1 推流 0 手机直播")
        private Integer liveType;

        @Schema(description = "是否关闭点赞 【0：开启，1：关闭】（若关闭，观众端将隐藏点赞按钮，直播开始后不允许开启）")
        private Integer closeLike;

        @Schema(description = "是否关闭货架 【0：开启，1：关闭】（若关闭，观众端将隐藏货架，直播开始后不允许开启）")
        private Integer closeGoods;

        @Schema(description = "是否关闭评论 【0：开启，1：关闭】（若关闭，观众端将隐藏评论入口，直播开始后不允许开启）")
        private Integer closeComment;

        @Schema(description = "是否关闭客服 【0：开启，1：关闭】 默认关闭客服（直播开始后允许开启）")
        private Integer closeKf;

        @Schema(description = "是否关闭回放 【0：开启，1：关闭】默认关闭回放（直播开始后允许开启）")
        private Integer closeReplay;

        @Schema(description = "是否开启官方收录，1 开启，0 关闭")
        private Integer isFeedsPublic;

        @Schema(description = "创建者openid")
        private String creatorOpenid;

        @Schema(description = "官方收录封面")
        private String feedsImg;

        @Schema(description = "商品信息列表")
        private List<ProductInfo> goods;
    }

}
