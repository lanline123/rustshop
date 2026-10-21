// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 直播间详细信息响应
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "直播间详细信息响应")
public class RoomInfoResult {

    @Schema(description = "返回码")
    private Long errcode;

    @Schema(description = "错误信息")
    private String errmsg;

    @Schema(description = "房间信息")
    private RoomInfo roomInfo;

    @Data
    @Schema(description = "房间信息")
    public static class RoomInfo {
        @Schema(description = "房间ID")
        private Integer roomId;

        @Schema(description = "房间名称")
        private String name;

        @Schema(description = "背景图")
        private String coverImg;

        @Schema(description = "直播间开始时间")
        private Long startTime;

        @Schema(description = "直播计划结束时间")
        private Long endTime;

        @Schema(description = "主播名")
        private String anchorName;

        @Schema(description = "主播微信号")
        private String anchorWechat;

        @Schema(description = "直播间状态：101-直播中，102-未开始，103-已结束，104-禁播，105-暂停，106-异常，107-已过期")
        private Integer status;

        @Schema(description = "分享图")
        private String shareImg;

        @Schema(description = "直播类型：1-推流，0-手机直播")
        private Integer liveType;

        @Schema(description = "是否关闭点赞：0-开启，1-关闭")
        private Integer closeLike;

        @Schema(description = "是否关闭货架：0-开启，1-关闭")
        private Integer closeGoods;

        @Schema(description = "是否关闭评论：0-开启，1-关闭")
        private Integer closeComment;

        @Schema(description = "是否关闭客服：0-开启，1-关闭")
        private Integer closeKf;

        @Schema(description = "是否关闭回放：0-开启，1-关闭")
        private Integer closeReplay;

        @Schema(description = "是否开启官方收录：1-开启，0-关闭")
        private Integer isFeedsPublic;

        @Schema(description = "官方收录封面")
        private String feedsImg;

        @Schema(description = "创建者openid")
        private String creatorOpenid;

        @Schema(description = "商品信息列表")
        private List<ProductInfo> goods;
    }

    @Data
    @Schema(description = "商品信息")
    public static class ProductInfo {
        @Schema(description = "商品名称")
        private String name;

        @Schema(description = "商品封面图链接")
        private String coverImg;

        @Schema(description = "商品小程序路径")
        private String url;

        @Schema(description = "商品价格（分）")
        private Long price;

        @Schema(description = "价格类型：1-一口价，2-价格区间，3-显示折扣价")
        private Integer priceType;

        @Schema(description = "商品ID")
        private Integer goodsId;

        @Schema(description = "第三方商品appid，当前小程序商品则为空")
        private String thirdPartyAppid;
    }
}
