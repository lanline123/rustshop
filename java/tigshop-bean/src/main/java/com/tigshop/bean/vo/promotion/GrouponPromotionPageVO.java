package com.tigshop.bean.vo.promotion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.enums.promotion.PromotionTimeStatusEnum;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拼团分页列表
 *
 * @author kidd
 * @since 2025/8/28 15:03
 */
@Data
public class GrouponPromotionPageVO {

    @Schema(description = "拼团 ID")
    private Long grouponPromotionId;

    @Schema(description = "活动名称")
    private String name;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "开始时间")
    private String startTime;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @Schema(description = "所属店铺")
    private Integer shopId;

    // *** Other ***

    @Schema(description = "活动状态；1-活动进行中，2-活动已结束，3-活动未开始")
    private Integer status;

    @Schema(description = "活动状态；1-活动进行中，2-活动已结束，3-活动未开始")
    private String statusName;

    @Schema(description = "所属店铺")
    private String shopName;

    @Schema(description = "应收金额")
    private BigDecimal receiveAmount;

    @Schema(description = "成团数量")
    private Long groupSuccessCount;

    @Schema(description = "支付单数")
    private Long payOrderCount;

    public void assemble(List<GrouponPromotionRecord> grouponPromotionRecords, Shop shop) {
        long startTime = Long.parseLong(this.startTime);
        long endTime = Long.parseLong(this.endTime);
        this.statusName = PromotionTimeStatusEnum.handleStatusName(startTime, endTime);

        long currentTime = StringUtils.getCurrentTime();
        if (currentTime >= startTime && currentTime < endTime) {
            this.status = PromotionTimeStatusEnum.PROMOTION_STATUS_ON.getCode();
        } else if (currentTime < startTime) {
            this.status = PromotionTimeStatusEnum.PROMOTION_STATUS_FORTHCOMING.getCode();
        } else {
            this.status = PromotionTimeStatusEnum.PROMOTION_STATUS_OFF.getCode();
        }

        if (shop != null) {
            this.shopName = shop.getShopTitle();
        }

        this.payOrderCount = (long) grouponPromotionRecords.size();

        List<GrouponPromotionRecord> grouponSuccessRecords = grouponPromotionRecords.stream()
                .filter(item -> item.getJoinStatus().equals(2))
                .toList();

        this.receiveAmount = grouponSuccessRecords.stream()
                .map(GrouponPromotionRecord::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.groupSuccessCount = grouponSuccessRecords.stream()
                .filter(item -> item.getJoinType().equals(1))
                .count();
    }
}
