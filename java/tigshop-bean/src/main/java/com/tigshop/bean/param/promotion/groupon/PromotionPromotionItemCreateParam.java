package com.tigshop.bean.param.promotion.groupon;

import com.alibaba.fastjson.JSONArray;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.bean.model.promotion.GrouponPromotionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼团活动商品新增参数
 *
 * @author kidd
 * @since 2025/8/28 18:03
 */
@Data
public class PromotionPromotionItemCreateParam {

    @NotNull(message = "拼团活动 ID 不能为空")
    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Long productId;

    @NotNull(message = "拼团类型不能为空")
    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @NotNull(message = "每单限购数量不能为空")
    @Schema(description = "每单限购数量；0-不限购")
    private Integer orderLimitNum;

    @NotNull(message = "是否自动成团不能为空")
    @Schema(description = "是否自动成团；0-否，1-是")
    private Integer isAutoGroup;

    @NotNull(message = "成团最低人数不能为空")
    @Schema(description = "成团最低人数")
    private Integer minGroupNum;

    @Schema(description = "成团最低价格")
    private BigDecimal minGroupPrice;

    @Schema(description = "排序")
    private Integer sort;

    // *** Other ***

    @Valid
    @NotEmpty(message = "成团人数集合不能为空")
    @Schema(description = "成团人数集合")
    private List<GrouponNumParam> groupNums;

    @Valid
    @NotEmpty(message = "拼团信息不能为空")
    @Schema(description = "拼团信息")
    private List<GrouponPromotionDetailParam> grouponPromotionDetails;

    @Data
    public static class GrouponNumParam {

        @NotNull(message = "成团人数 key 不能为空")
        @Schema(description = "成团人数 key")
        private Integer groupNumKey;

        @NotNull(message = "成团人数不能为空")
        @Schema(description = "成团人数")
        private Integer groupNum;

    }

    @Data
    public static class GrouponPromotionDetailParam {

        @Schema(description = "ID")
        private Long grouponDetailId;

        @NotNull(message = "商品ID不能为空")
        @Schema(description = "商品ID")
        private Long productId;

        @NotNull(message = "商品规格 id不能为空")
        @Schema(description = "商品规格 id")
        private Long skuId;

        @NotNull(message = "可售数量不能为空")
        @Schema(description = "可售数量")
        private Integer limitNum;

        @NotNull(message = "是否参加不能为空")
        @Schema(description = "是否参加；0-否，1-是")
        private Integer isJoin;

        // *** Other ***

        @Valid
        @NotEmpty(message = "拼团价不能为空")
        @Schema(description = "拼团价")
        private List<GrouponPromotionDetailPriceParam> groupPromotionDetailPrices;

        @Data
        public static class GrouponPromotionDetailPriceParam {

            @NotNull(message = "成团人数 key 不能为空")
            @Schema(description = "成团人数 key")
            private Integer groupNumKey;

            @NotNull(message = "拼团价不能为空")
            @Schema(description = "拼团价")
            private BigDecimal price;
        }

        public GrouponPromotionDetail buildGrouponPromotionDetail(Long grouponPromotionId, Long grouponPromotionItemId) {

            String groupPrice = JSONArray.toJSONString(this.groupPromotionDetailPrices);

            return GrouponPromotionDetail.builder()
                    .grouponDetailId(this.grouponDetailId)
                    .grouponPromotionId(grouponPromotionId)
                    .grouponPromotionItemId(grouponPromotionItemId)
                    .productId(this.productId)
                    .skuId(this.skuId)
                    .groupPrice(groupPrice)
                    .limitNum(this.limitNum)
                    .isJoin(this.isJoin)
                    .addTime(Instant.now().toEpochMilli())
                    .build();
        }

    }

    public GrouponPromotionItem buildGrouponPromotionItem() {

        String groupNum = JSONArray.toJSONString(this.groupNums);

        return GrouponPromotionItem.builder()
                .grouponPromotionId(this.grouponPromotionId)
                .productId(this.productId)
                .groupType(this.groupType)
                .groupNum(groupNum)
                .orderLimitNum(this.orderLimitNum)
                .isAutoGroup(this.isAutoGroup)
                .minGroupNum(this.minGroupNum)
                .minGroupPrice(this.minGroupPrice)
                .sort(this.sort)
                .addTime(Instant.now().toEpochMilli())
                .build();
    }

    public List<GrouponPromotionDetail> buildGrouponPromotionDetails(Long grouponPromotionItemId) {
        return this.grouponPromotionDetails.stream()
                .map(detail -> detail.buildGrouponPromotionDetail(this.grouponPromotionId, grouponPromotionItemId))
                .collect(Collectors.toList());
    }
}
