package com.tigshop.bean.vo.promotion;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.bean.model.promotion.GrouponPromotionItem;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼团活动商品详情
 *
 * @author kidd
 * @since 2025/8/28 18:04
 */
@Data
public class GrouponPromotionItemDetailVO {

    @Schema(description = "拼团活动商品 ID")
    private Long grouponPromotionItemId;

    @Schema(description = "拼团活动 ID")
    private Long grouponPromotionId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "sku信息")
    private List<ProductSkuDTO> productSkus;

    @Schema(description = "商品原价")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal productPrice;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "拼团类型；1-普通团，2-阶梯团")
    private Integer groupType;

    @Schema(description = "每单限购数量；0-不限购")
    private Integer orderLimitNum;

    @Schema(description = "是否自动成团；0-否，1-是")
    private Integer isAutoGroup;

    @Schema(description = "成团最低人数")
    private Integer minGroupNum;

    @Schema(description = "排序")
    private Integer sort;

    // *** Other ***

    @Schema(description = "成团人数集合")
    private List<GrouponNumVO> groupNums;

    @Schema(description = "拼团信息")
    private List<GrouponPromotionDetailVO> grouponPromotionDetails;

    public GrouponPromotionItemDetailVO(GrouponPromotionItem dbGrouponPromotionItem, List<GrouponPromotionDetail> dbGrouponPromotionDetails) {
        BeanUtil.copyProperties(dbGrouponPromotionItem, this);
        this.groupNums = JSONArray.parseArray(dbGrouponPromotionItem.getGroupNum(), GrouponNumVO.class);

        this.grouponPromotionDetails = dbGrouponPromotionDetails.stream().map(GrouponPromotionDetailVO::new).collect(Collectors.toList());
    }

    @Data
    public static class GrouponNumVO {

        @Schema(description = "成团人数 key")
        private Integer groupNumKey;

        @Schema(description = "成团人数")
        private Integer groupNum;

    }

    @Data
    public static class GrouponPromotionDetailVO {

        @Schema(description = "ID")
        private Long grouponDetailId;

        @Schema(description = "商品ID")
        private Long productId;

        @Schema(description = "商品规格 id")
        private Long skuId;

        @Schema(description = "sku信息")
        private ProductSkuDTO productSku;

        @Schema(description = "可售数量")
        private Integer limitNum;

        @Schema(description = "是否参加；0-否，1-是")
        private Integer isJoin;

        // *** Other ***

        @Schema(description = "拼团价")
        private List<GrouponPromotionDetailPriceVO> groupPromotionDetailPrices;

        @Data
        public static class GrouponPromotionDetailPriceVO {

            @Schema(description = "成团人数 key")
            private Integer groupNumKey;

            @Schema(description = "拼团价")
            private BigDecimal price;
        }

        public GrouponPromotionDetailVO(GrouponPromotionDetail grouponPromotionDetail) {
            BeanUtil.copyProperties(grouponPromotionDetail, this);
            this.groupPromotionDetailPrices = JSONArray.parseArray(grouponPromotionDetail.getGroupPrice(), GrouponPromotionDetailPriceVO.class);
        }

    }

}
