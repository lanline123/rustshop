package com.tigshop.bean.vo.promotion;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.GrouponPromotion;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.bean.model.promotion.GrouponPromotionItem;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拼团活动商品
 *
 * @author kidd
 * @since 2025/8/29 14:02
 */
@Data
public class GrouponItemVO {

    @Schema(description = "拼团活动商品 ID")
    private Long grouponPromotionItemId;

    @Schema(description = "商品ID")
    private Long productId;

    // *** Other ***

    @Schema(description = "最低拼团价格")
    private BigDecimal minGrouponPrice;

    @Schema(description = "最小成团人数")
    private Integer minGroupNum;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品简介")
    private String productBrief;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "缩略图URL")
    @JsonTranslate(dataType = 7)
    private String picThumb;

    @Schema(description = "商品图")
    @JsonTranslate(dataType = 7)
    private String picUrl;

    @Schema(description = "参团人数")
    private Long joinNumCount;

    @Schema(description = "成团人数（json）")
    private String groupNum;

    @Schema(description = "活动开始时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String startTime;

    @Schema(description = "活动结束时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String endTime;

    @Schema(description = "当前用户是否可参与（二期会员标签人群；false 时前台展示价格但置灰拼团按钮）")
    private Boolean canJoin = true;

    @Schema(description = "不可参与提示文案，可参与时为空")
    private String restrictTip;

    public GrouponItemVO(GrouponPromotion grouponPromotion, GrouponPromotionItem item, Product product, List<GrouponPromotionRecord> grouponRecords, List<GrouponPromotionDetail> grouponPromotionDetails) {
        this.grouponPromotionItemId = item.getGrouponPromotionItemId();
        this.productId = item.getProductId();
        this.groupNum = item.getGroupNum();
        this.minGroupNum = item.getMinGroupNum();

        if (grouponPromotion != null) {
            this.startTime = grouponPromotion.getStartTime().toString();
            this.endTime = grouponPromotion.getEndTime().toString();
        }

        if (product != null) {
            this.productName = product.getProductName();
            this.productPrice = product.getProductPrice();
            this.picThumb = product.getPicThumb();
            this.picUrl = product.getPicUrl();
            this.productBrief = product.getProductBrief();
            this.productSn = product.getProductSn();
        }

        this.joinNumCount = (long) grouponRecords.size();

        this.minGrouponPrice = grouponPromotionDetails.stream()
                .flatMap(detail ->
                        JSONArray.parseArray(detail.getGroupPrice(), GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO.class).stream()
                )
                .map(GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO::getPrice)
                .min(BigDecimal::compareTo)
                .get();
    }
}
