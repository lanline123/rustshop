package com.tigshop.bean.vo.promotion;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拼团活动商品分页列表
 *
 * @author kidd
 * @since 2025/8/28 18:04
 */
@Data
public class GrouponPromotionItemPageVO {

    @Schema(description = "拼团活动商品 ID")
    private Long grouponPromotionItemId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "成团人数（json）")
    private String groupNum;

    @Schema(description = "排序")
    private Integer sort;

    // *** Other ***

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "大图")
    private String picLarge;

    @Schema(description = "商品主图URL")
    @JsonTranslate(dataType = 7)
    private String picUrl;

    @Schema(description = "商品价格")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal productPrice;

    @Schema(description = "最低拼团价")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal minGroupPrice;

    @Schema(description = "最大拼团价")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal maxGroupPrice;

    @Schema(description = "最低团规模")
    private Integer minGroupNum;

    @Schema(description = "最大团规模")
    private Integer maxGroupNum;

    @Schema(description = "最小可售数量")
    private Integer minLimitNum;

    @Schema(description = "最大可售数量")
    private Integer maxLimitNum;

    @Schema(description = "商品描述")
    private String productBrief;

    public void assembleData(List<GrouponPromotionDetail> grouponPromotionDetails) {
        this.minGroupPrice = grouponPromotionDetails.stream()
                .flatMap(detail ->
                        JSONArray.parseArray(detail.getGroupPrice(), GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO.class).stream()
                )
                .map(GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO::getPrice)
                .min(BigDecimal::compareTo)
                .get();

        this.maxGroupPrice = grouponPromotionDetails.stream()
                .flatMap(detail ->
                        JSONArray.parseArray(detail.getGroupPrice(), GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO.class).stream()
                )
                .map(GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO::getPrice)
                .max(BigDecimal::compareTo)
                .get();

        List<GrouponPromotionItemDetailVO.GrouponNumVO> grouponNums = JSONArray.parseArray(this.groupNum, GrouponPromotionItemDetailVO.GrouponNumVO.class);
        this.minGroupNum = grouponNums.stream()
                .map(GrouponPromotionItemDetailVO.GrouponNumVO::getGroupNum)
                .min(Integer::compareTo)
                .get();

        this.maxGroupNum = grouponNums.stream()
                .map(GrouponPromotionItemDetailVO.GrouponNumVO::getGroupNum)
                .max(Integer::compareTo)
                .get();

        this.minLimitNum = grouponPromotionDetails.stream()
                .map(GrouponPromotionDetail::getLimitNum)
                .min(Integer::compareTo)
                .get();

        this.maxLimitNum = grouponPromotionDetails.stream()
                .map(GrouponPromotionDetail::getLimitNum)
                .max(Integer::compareTo)
                .get();
    }
}
