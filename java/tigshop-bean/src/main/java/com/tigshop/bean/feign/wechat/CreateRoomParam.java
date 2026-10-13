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

/**
 * 创建直播间参数
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "创建直播间参数")
public class CreateRoomParam {

    @Schema(description = "房间名称")
    private String name;

    @Schema(description = "背景图")
    private String coverImg;

    @Schema(description = "分享图")
    private String shareImg;

    @Schema(description = "开始时间（Unix时间戳，秒）")
    private Long startTime;

    @Schema(description = "结束时间（Unix时间戳，秒）")
    private Long endTime;

    @Schema(description = "主持人姓名")
    private String anchorName;

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

    @Schema(description = "是否开启官方收录：0-关闭，1-开启")
    private Integer isFeedsPublic;

    @Schema(description = "官方收录封面")
    private String feedsImg;

    @Schema(description = "主播ID")
    private String anchorId;

    @Schema(description = "主播微信openid")
    private String anchorOpenid;

    @Schema(description = "副主播微信账号")
    private String subAnchorWechat;
}
