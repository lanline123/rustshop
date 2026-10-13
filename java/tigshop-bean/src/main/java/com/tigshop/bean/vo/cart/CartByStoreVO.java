// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.cart;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.model.order.OrderAmountDetail;
import com.tigshop.bean.model.order.OrderCouponDetail;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车单个店铺里数据
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "购物车数据")
public class CartByStoreVO {

    @Schema(description = "统计数据(改价逻辑后,这个数据无效)")
    private CartStoreTotal total;

    @Schema(description = "该店铺的商品")
    private List<CartVO> carts;

    @Schema(description = "优惠")
    private List<PromotionVO> usedPromotions;

    @Schema(description = "允许的优惠")
    private List<PromotionVO> enableUsePromotion;

    @Schema(description = "增品")
    private List<CartPromotionParsePriceDTO.Gift> gift;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "是否无需配送；0-否，1-是")
    private Integer noShipping;

    //是否未达到起送价
    @Schema(description = "是否未达到起送价")
    private Boolean isNotReachMinOrderAmount;
    //起送价差额
    @Schema(description = "起送价差额")
    private BigDecimal minOrderAmountDiff;

    @Schema(description = "B2B批发：组级（SPU 起批量未达标）提示；null=无提示")
    private String wholesaleGroupHint;

    @Schema(description = "B2B批发：该店铺内是否有批发能力商品")
    private Boolean wholesaleConfigured;

    @Data
    @Schema(description = "单个店铺的统计数据")
    public static class CartStoreTotal {

        @Schema(description = "商品价格（已选）")
        private List<Integer> couponIds;

        @Schema(description = "优惠券优惠金额")
        private BigDecimal discountCouponAmount;

        @Schema(description = "商品营销金额")
        private BigDecimal discountProductPromotionAmount;

        @Schema(description = "秒杀优惠金额")
        private BigDecimal discountSeckillAmount;

        @Schema(description = "限时折扣优惠金额")
        private BigDecimal discountTimeDiscountAmount;

        @Schema(description = "拼团优惠金额")
        private BigDecimal discountGroupBuyAmount;

        @Schema(description = "优惠后")
        private BigDecimal discountDiscountAmount;

        @Schema(description = "改价优惠金额")
        private BigDecimal discountChangePrice;

        @Schema(description = "优惠 组合商品（已选）")
        private BigDecimal discounts;

        //初始化数据为0
        public CartStoreTotal() {
            this.discountCouponAmount = BigDecimal.ZERO;
            this.discountProductPromotionAmount = BigDecimal.ZERO;
            this.discountSeckillAmount = BigDecimal.ZERO;
            this.discountTimeDiscountAmount = BigDecimal.ZERO;
            this.discountChangePrice = BigDecimal.ZERO;
            this.discounts = BigDecimal.ZERO;
            this.couponIds = new ArrayList<>();
        }
    }

    //初始化total
    public CartByStoreVO() {
        this.total = new CartStoreTotal();
        this.carts = new ArrayList<>();
        this.usedPromotions = new ArrayList<>();
        this.gift = new ArrayList<>();
    }

    public OrderAmountDetail creatOrderAmountDetail(OrderCheckVO.Total totalFee) {
        return OrderAmountDetail.builder()
                .orderId(0)
                .shopId(this.shopId)
                .shippingFee(totalFee.getShippingFeeByShopId(this.shopId))
                .discountAmount(this.total.getDiscounts())
                .couponAmount(this.total.getDiscountCouponAmount())
                .timeDiscountAmount(this.total.getDiscountTimeDiscountAmount())
                .build();
    }

    public List<OrderCouponDetail> createOrderCouponDetails() {
        if (CollUtil.isEmpty(this.total.getCouponIds())) {
            return Collections.emptyList();
        }

        return this.total.getCouponIds().stream()
                .map(couponId -> OrderCouponDetail.builder()
                        .orderId(0)
                        .couponId(couponId)
                        .shopId(this.shopId)
                        .build()
                ).collect(Collectors.toList());
    }

    public void clearPromotion(){
//        setEnableUsePromotion(null);
//        setUsedPromotions(null);
    }

}