package com.tigshop.bean.vo.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 拼团推广
 *
 * @author kidd
 * @since 2025/9/3 09:26
 */
@Data
@Builder
public class GrouponPostVO {
    @Schema(description = "活动名称")
    private String promotionName;

    @Schema(description = "活动类型")
    private String promotionType;

    @Schema(description = "活动图片")
    private String promotionPicUrl;

    @Schema(description = "WeChat短链")
    private String wechatShortLink;

    @Schema(description = "短链")
    private String shortLink;

    @Schema(description = "原链")
    private String originalLink;

    @Schema(description = "活动描述")
    private String promotionBrief;

    @Schema(description = "活动时间")
    private String promotionDateTime;

    @Schema(description = "小程序二维码")
    private String miniCode;
}
