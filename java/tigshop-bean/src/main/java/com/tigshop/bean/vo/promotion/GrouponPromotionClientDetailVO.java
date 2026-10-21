package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拼团活动客户端详情
 *
 * @author kidd
 * @since 2025/8/29 14:08
 */
@Data
public class GrouponPromotionClientDetailVO {

    @Schema(description = "拼团 ID")
    private Long grouponPromotionId;

    @Schema(description = "h5活动图片")
    @JsonTranslate(dataType = 7)
    private String grouponImage;

    @Schema(description = "pc活动图片")
    @JsonTranslate(dataType = 7)
    private String pcGrouponImage;

    @Schema(description = "开始时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String startTime;

    @Schema(description = "结束时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String endTime;

    @Schema(description = "活动说明")
    @JsonTranslate
    private String grouponExplain;

    @Schema(description = "所属店铺")
    private Integer shopId;

    // *** Other ***

    @Schema(description = "店铺名称")
    @JsonTranslate(dataType = 2)
    private String shopTitle;

    @Schema(description = "当前用户是否可参与（二期会员标签人群；false 时前台展示价格但置灰开团/参团按钮）")
    private Boolean canJoin;

    @Schema(description = "不可参与提示文案，可参与时为空")
    private String restrictTip;
}
