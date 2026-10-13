package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拼团活动商品详情
 *
 * @author kidd
 * @since 2025/9/1 11:25
 */
@Data
public class GrouponItemInfoVO {

    @Schema(description = "拼团活动商品 ID")
    private Long grouponPromotionItemId;

    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品sn")
    private String productSn;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @Schema(description = "成团人数（json）")
    private List<GroupNumItem> groupNums;

    @Schema(description = "每单限购数量；0-不限购")
    private Integer orderLimitNum;

    @Schema(description = "是否自动成团；0-否，1-是")
    private Integer isAutoGroup;

    @Schema(description = "成团最低人数")
    private Integer minGroupNum;

    // *** GrouponPromotion ***

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "开始时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String startTime;

    @Schema(description = "结束时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String endTime;

    @Schema(description = "成团有效时间")
    private Long expirationTime;

    @Schema(description = "h5活动图片")
    @JsonTranslate(dataType = 7)
    private String grouponImage;

    @Schema(description = "pc活动图片")
    @JsonTranslate(dataType = 7)
    private String pcGrouponImage;

    @Schema(description = "分享图")
    @JsonTranslate(dataType = 7)
    private String shareImage;

    @Schema(description = "活动说明")
    private String grouponExplain;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "最低拼团价格")
    private BigDecimal minGrouponPrice;

    @Schema(description = "成团数量")
    private Long groupSuccessCount;

    @Schema(description = "参团人数")
    private Long joinNumCount;
    // *** Other ***

    @Schema(description = "拼团信息")
    private List<GrouponPromotionDetail> grouponPromotionDetails;

    @Schema(description = "当前用户是否可参与（二期会员标签人群；false 时前台展示价格但置灰拼团按钮）")
    private Boolean canJoin = true;

    @Schema(description = "不可参与提示文案，可参与时为空")
    private String restrictTip;

    @Data
    public static class GroupNumItem {
        @Schema(description = "成团人数")
        private Integer groupNum;

        @Schema(description = "成团人数Key")
        private Integer groupNumKey;
    }
}
