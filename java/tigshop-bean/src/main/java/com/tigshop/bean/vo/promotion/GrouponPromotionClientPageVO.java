package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.enums.promotion.PromotionTimeStatusEnum;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户端拼团分页列表
 *
 * @author kidd
 * @since 2025/8/28 15:03
 */
@Data
public class GrouponPromotionClientPageVO {

    @Schema(description = "拼团 ID")
    private Long grouponPromotionId;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "开始时间")
    private Long startTime;

    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @Schema(description = "所属店铺")
    private Integer shopId;

    // *** Other ***

    @Schema(description = "开始时间")
    private String startTimeStr;

    @Schema(description = "结束时间")
    private String endTimeStr;

    @Schema(description = "活动状态；1-活动进行中，2-活动已结束，3-活动未开始")
    private Integer status;

    @Schema(description = "活动状态；1-活动进行中，2-活动已结束，3-活动未开始")
    private String statusName;

    @Schema(description = "所属店铺")
    private String shopName;

    public void assemble(Shop shop) {
        this.startTimeStr = TigUtils.handelTime(this.startTime);
        this.endTimeStr = TigUtils.handelTime(this.endTime);

        this.statusName = PromotionTimeStatusEnum.handleStatusName(this.startTime, this.endTime);

        long currentTime = StringUtils.getCurrentTime();
        if (currentTime >= startTime && currentTime <= endTime) {
            this.status = PromotionTimeStatusEnum.PROMOTION_STATUS_ON.getCode();
        } else if (currentTime < startTime) {
            this.status = PromotionTimeStatusEnum.PROMOTION_STATUS_FORTHCOMING.getCode();
        } else {
            this.status = PromotionTimeStatusEnum.PROMOTION_STATUS_OFF.getCode();
        }

        if (shop != null) {
            this.shopName = shop.getShopTitle();
        }

    }
}
