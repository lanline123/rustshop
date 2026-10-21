// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.content.WechatLiveConstants.WECHAT_LIVE_ID_NOT_NULL;

/**
  * 微信直播更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "微信直播参数")
public class WechatLiveUpdateDTO  {
    @Schema(description = "微信直播ID")
    @NotNull(message = WECHAT_LIVE_ID_NOT_NULL)
    private Integer wechatLiveId;

    @Schema(description ="微信直播名称")
    private String wechatLiveTitle;

    @Schema(description = "活动范围")
    private Integer actRange;

    @Schema(description = "活动简介")
    private List<Integer> actRangeExt;

    @Schema(description = "是否开启官方收录：0-关闭，1-开启")
    private Integer isFeedsPublic;

    @Schema(description = "直播间名称", required = true)
    private String name;

    @Schema(description = "背景图")
    private String coverImg;

    @Schema(description = "分享图")
    private String shareImg;

    @Schema(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @Schema(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @Schema(description = "主持人姓名")
    private String anchorName;

    @Schema(description = "主持人图片")
    private String anchorImg;

    @Schema(description = "直播间类型：0-手机直播，1-推流")
    private Integer liveType;

    @Schema(description = "是否关闭点赞：0-开启，1-关闭")
    private Integer closeLike;

    @Schema(description = "是否关闭货架：0-开启，1-关闭")
    private Integer closeGoods;

    @Schema(description = "是否关闭评论：0-开启，1-关闭")
    private Integer closeComment;

    @Schema(description = "是否关闭客服：0-开启，1-关闭")
    private Integer closeKf;

    @Schema(description = "是否开启回放：0-关闭，1-开启")
    private Integer closeReplay;

    @Schema(description = "官方收录封面")
    private String feedsImg;

    @Schema(description = "主播ID")
    private String anchorId;

    @Schema(description = "主播微信openid")
    private String anchorOpenid;

    @Schema(description = "副主播微信账号")
    private String subAnchorWechat;
}
