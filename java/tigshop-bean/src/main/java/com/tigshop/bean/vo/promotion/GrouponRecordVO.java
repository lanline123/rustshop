package com.tigshop.bean.vo.promotion;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.enums.promotion.JoinStatusEnum;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.bean.model.promotion.GrouponPromotionRecord;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.config.TimestampToDateSerializer;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 拼团活动记录列表
 *
 * @author kidd
 * @since 2025/8/29 15:44
 */
@Data
public class GrouponRecordVO {

    @Schema(description = "拼团记录 ID")
    private Long grouponPromotionRecordId;

    @Schema(description = "拼团活动商品 ID")
    private Long grouponPromotionItemId;

    @Schema(description = "拼团活动id")
    private Long grouponPromotionId;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "拼团状态；1-待成团，2-拼团成功，3-拼团失败")
    private Integer joinStatus;

    @Schema(description = "拼团状态desc")
    @JsonTranslate
    private String joinStatusDesc;

    @Schema(description = "成团最低人数")
    private Integer minGroupNum;

    // *** Other ***

    @Schema(description = "最低拼团价格")
    private BigDecimal minGrouponPrice;

    @Schema(description = "拼团价格")
    private BigDecimal grouponPrice;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品sn")
    private String productSn;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "参团人数")
    private Long joinNumCount;

    @Schema(description = "开始时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String startTime;

    @Schema(description = "结束时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String endTime;

    @Schema(description = "参团用户")
    private GrouponRecordDetailVO.JoinUser joinUser;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "订单号")
    private Integer orderId;

    @Schema(description = "下单时间")
    private String orderAddTime;

    @Schema(description = "支付方式")
    private Integer payTypeId;

    public GrouponRecordVO(GrouponPromotionRecord item, Product product, List<GrouponPromotionRecord> grouponRecords,
                           List<GrouponPromotionDetail> grouponPromotionDetails, User user, Order order) {
        this.grouponPromotionRecordId = item.getGrouponPromotionRecordId();
        this.grouponPromotionItemId = item.getGrouponPromotionItemId();
        this.productName = item.getProductName();
        this.joinStatus = item.getJoinStatus();
        this.joinStatusDesc = JoinStatusEnum.getDescription(item.getJoinStatus());
        this.minGroupNum = item.getMinGroupNum();
        this.joinUser = new GrouponRecordDetailVO.JoinUser((long) user.getUserId(), user.getUsername(), user.getAvatar(), user.getUsername().equals(item.getGroupCaptainName()));
        this.startTime = item.getOpenGroupTime().toString();
        this.endTime = item.getJoinExpirationTime().toString();
        this.grouponPromotionId = item.getGrouponPromotionId();
        this.orderSn = item.getOrderSn();
        this.orderId = Optional.ofNullable(item.getOrderId()).orElse(0L).intValue();
        this.orderAddTime = TigUtils.handelTime(order.getAddTime());
        this.payTypeId = order.getPayTypeId();

        if (product != null) {
            this.productPrice = product.getProductPrice();
            this.picThumb = product.getPicThumb();
            this.productSn = product.getProductSn();
        }

        this.joinNumCount = (long) grouponRecords.size();

        this.grouponPrice = item.getPaidAmount();
        this.minGrouponPrice = grouponPromotionDetails.stream()
                .flatMap(detail ->
                        JSONArray.parseArray(detail.getGroupPrice(),
                                GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO.class
                        ).stream()
                )
                .map(GrouponPromotionItemDetailVO.GrouponPromotionDetailVO.GrouponPromotionDetailPriceVO::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

}
