// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 微信直播VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "微信直播参数")
public class WechatLiveVO {
    @Schema(description = "直播ID")
    private Integer wechatLiveId;

    @Schema(description = "直播标题")
    private String wechatLiveTitle;

    @Schema(description = "直播内容")
    private String wechatLiveData;

    @Schema(description = "活动范围")
    private Integer actRange;

    @Schema(description = "活动简介")
    private List<Integer> actRangeExt;

    @Schema(description = "房间号")
    private Integer roomId;

    @Schema(description = "分享图")
    private String shareImg;

    @Schema(description = "直播类型：0-手机直播，1-推流")
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

    @Schema(description = "是否开启官方收录：0-关闭，1-开启")
    private Integer isFeedsPublic;

    @Schema(description = "官方收录封面")
    private String feedsImg;

    @Schema(description = "封面图")
    private String coverImg;

    @Schema(description = "主播ID")
    private String anchorId;

    @Schema(description = "主播微信openid")
    private String anchorOpenid;

    @Schema(description = "副主播微信账号")
    private String subAnchorWechat;

    @Schema(description = "主持人姓名")
    private String anchorName;

    @Schema(description = "主持人图片")
    private String anchorImg;

    @Schema(description = "创建者openid")
    private String creatorOpenid;

    @Schema(description = "直播状态")
    private Integer liveStatus;

    @Schema(description = "直播状态文本")
    private String liveStatusText;

    @Schema(description = "审核状态：0-未审核，1-审核中，2-审核通过，3-审核驳回")
    private Integer auditStatus;

    @Schema(description = "审核状态文本")
    private String auditStatusText;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "最后一次更新时间")
    private String lastUpdateTime;

    @Schema(description = "产品数据")
    private List<Integer> productData;

    @Schema(description = "缩略图")
    private String thumbImg;

    @Schema(description = "直播编码")
    private String liveSn;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "关联订单数")
    private Long orderCount;

    @Schema(description = "同步失败数")
    private Long syncFailedCount;
}