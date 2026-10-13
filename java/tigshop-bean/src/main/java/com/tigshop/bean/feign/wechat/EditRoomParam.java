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
 * 编辑直播间参数
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "编辑直播间参数")
public class EditRoomParam {

    @Schema(description = "房间ID")
    private Integer id;

    @Schema(description = "房间名称")
    private String name;

    @Schema(description = "背景图")
    private String coverImg;

    @Schema(description = "分享图")
    private String shareImg;

    @Schema(description = "是否关闭点赞：0-开启，1-关闭")
    private Integer closeLike;

    @Schema(description = "是否关闭货架：0-开启，1-关闭")
    private Integer closeGoods;

    @Schema(description = "是否关闭评论：0-开启，1-关闭")
    private Integer closeComment;

    @Schema(description = "是否关闭客服：0-开启，1-关闭")
    private Integer closeKf;

    @Schema(description = "是否开启官方收录：0-关闭，1-开启")
    private Integer isFeedsPublic;

    @Schema(description = "官方收录封面")
    private String feedsImg;
}
