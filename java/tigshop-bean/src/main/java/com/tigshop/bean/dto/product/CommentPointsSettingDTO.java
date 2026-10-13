package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评价管理分值设置DTO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Schema(description = "评价管理分值设置DTO")
@Data
public class CommentPointsSettingDTO {
    @Schema(description = "id")
    private Integer useIntegral;

    @Schema(description = "名称")
    private String integralName;

    @Schema(description = "积分比例")
    private Integer integralScale;

    @Schema(description = "积分百分比")
    private Integer integralPercent;

    @Schema(description = "评论发送点")
    private Integer commentSendPoint;

    @Schema(description = "显示发送点")
    private Integer showSendPoint;

    @Schema(description = "使用签到")
    private Integer useQiandaoPoint;

    @Schema(description = "订单发送点")
    private Integer orderSendPoint;
}
