package com.tigshop.bean.vo.promotion;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.enums.promotion.JoinStatusEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.GrouponPromotion;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼团记录详情
 *
 * @author kidd
 * @since 2025/8/29 16:16
 */
@Data
public class GrouponRecordDetailVO {

    @Schema(description = "拼团记录 ID")
    private Long grouponPromotionRecordId;

    @Schema(description = "拼团活动id")
    private Long grouponPromotionId;

    @Schema(description = "活动itemId")
    private Long grouponPromotionItemId;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "商品 ID")
    private Integer productId;

    @Schema(description = "参与类型；1-开团，2-参团")
    private Integer joinType;

    @Schema(description = "拼团状态；1-待成团，2-拼团成功，3-拼团失败")
    private Integer joinStatus;

    @Schema(description = "拼团状态desc")
    @JsonTranslate
    private String joinStatusDesc;

    @Schema(description = "成团最低人数")
    private Integer minGroupNum;

    @Schema(description = "所属店铺")
    private Integer shopId;

    @Schema(description = "商品sn")
    private String productSn;

    // *** Other ***

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "h5活动图片")
    @JsonTranslate(dataType = 7)
    private String grouponImage;

    @Schema(description = "pc活动图片")
    @JsonTranslate(dataType = 7)
    private String pcGrouponImage;

    @Schema(description = "最低拼团价格")
    private BigDecimal minGrouponPrice;

    @Schema(description = "拼团价格")
    private BigDecimal grouponPrice;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "开始时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String startTime;

    @Schema(description = "结束时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String endTime;

    @Schema(description = "参团人数")
    private Long joinNumCount;

    @Schema(description = "订单id")
    private Long orderId;

    @Schema(description = "参团用户")
    private List<JoinUser> joinUsers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinUser {

        @Schema(description = "用户 ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "是否团长")
        private boolean leader;
    }

    public GrouponRecordDetailVO(GrouponPromotionRecord record, Shop shop, GrouponPromotion grouponPromotion,
                                 Product product, Long joinNumCount, List<User> users, List<GrouponPromotionDetail> grouponPromotionDetails) {
        BeanUtil.copyProperties(record, this);
        this.startTime = record.getOpenGroupTime().toString();
        this.endTime = record.getJoinExpirationTime().toString();
        this.joinStatusDesc = JoinStatusEnum.getDescription(record.getJoinStatus());
        this.orderId = record.getOrderId();
        this.grouponPromotionItemId = record.getGrouponPromotionItemId();
        this.grouponPrice = record.getPaidAmount();
        if (shop != null) {
            this.shopTitle = shop.getShopTitle();
            this.shopId = shop.getShopId();
        }

        if (grouponPromotion != null) {
            this.grouponImage = grouponPromotion.getGrouponImage();
            this.pcGrouponImage = grouponPromotion.getPcGrouponImage();
            this.grouponPromotionId = grouponPromotion.getGrouponPromotionId();
        }

        if (product != null) {
            this.productId = product.getProductId();
            this.productPrice = product.getProductPrice();
            this.picThumb = product.getPicThumb();
            this.productSn = product.getProductSn();
        }

        this.joinNumCount = joinNumCount;

        this.joinUsers = users.stream()
                .map(user -> new JoinUser(
                        (long) user.getUserId(),
                        user.getUsername(),
                        user.getAvatar(),
                        user.getUsername().equals(record.getGroupCaptainName())
                ))
                .collect(Collectors.toList());

        this.minGrouponPrice = grouponPromotionDetails.stream()
                .flatMap(detail ->
                        JSONArray.parseArray(detail.getGroupPrice(), GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO.class).stream()
                )
                .map(GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO::getPrice)
                .min(BigDecimal::compareTo)
                .get();
    }
}
