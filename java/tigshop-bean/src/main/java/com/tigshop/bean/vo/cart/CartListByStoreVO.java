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

import com.tigshop.bean.model.cart.CartGlobalChangePrice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 购物车数据
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "购物车数据")
public class CartListByStoreVO {
    @Schema(description = "统计数据")
    private CartTotal total;

    @Schema(description = "返回的数据是否有改价(存储到db的),为true的时候,cartGlobalChangePrice为空,说明是单个改价")
    private Boolean hasChangePrice;

    @Schema(description = "购物车全局改价数据")
    CartGlobalChangePrice cartGlobalChangePrice;

    @Schema(description = "示例名称")
    private List<CartByStoreVO> cartList;

    @Schema(description = "B2B批发：顶层；当前用户是否批发商")
    private Boolean isWholesaleBuyer;

    @Data
    @Schema(description = "统计数据")
    public static class CartTotal {
        @Schema(description = "商品价格（已选）")
        private BigDecimal productAmount;

        @Schema(description = "商品数（已选）")
        private Integer checkedCount;

        @Schema(description = "优惠 组合商品（已选）")
        private BigDecimal discounts;

        @Schema(description = "优惠后总价")
        private BigDecimal discountAfter;

        @Schema(description = "改价优惠(最终改价优惠,包含抵消其他优惠的部分)")
        private BigDecimal discountChangePrice;

        @Schema(description = "收银台除去优惠卷和改价的金额")
        private BigDecimal posOtherDiscountAmount;

        @Schema(description = "总商品数")
        private Integer totalCount;

        @Schema(description = "优惠券优惠金额")
        private BigDecimal discountCouponAmount;

        @Schema(description = "秒杀优惠金额")
        private BigDecimal discountSeckillAmount;

        @Schema(description = "满减优惠金额")
        private BigDecimal discountProductPromotionAmount;

        @Schema(description = "限时折扣优惠金额")
        private BigDecimal discountTimeDiscountAmount;

        @Schema(description = "拼团优惠金额")
        private BigDecimal discountGroupBuyAmount;

        // 包含了改价的优惠进去(改价优惠是进行其他优惠平摊的结果)
        @Schema(description = "其他优惠优惠金额")
        private BigDecimal discountDiscountAmount;

        @Schema(description = "抹分金额")
        private BigDecimal roundOffCentsAmount;

        @Schema(description = "抹角金额")
        private BigDecimal roundOffDimeAmount;

        @Schema(description = "附加费用")
        private BigDecimal serviceFee;

        //初始化所有值为0
        public CartTotal() {
            this.productAmount = BigDecimal.ZERO;
            this.checkedCount = 0;
            this.discounts = BigDecimal.ZERO;
            this.discountAfter = BigDecimal.ZERO;
            this.totalCount = 0;
            this.discountCouponAmount = BigDecimal.ZERO;
            this.discountSeckillAmount = BigDecimal.ZERO;
            this.discountProductPromotionAmount = BigDecimal.ZERO;
            this.discountTimeDiscountAmount = BigDecimal.ZERO;
            this.discountDiscountAmount = BigDecimal.ZERO;
            this.roundOffCentsAmount = BigDecimal.ZERO;
            this.roundOffDimeAmount = BigDecimal.ZERO;
            this.serviceFee = BigDecimal.ZERO;
        }

        public void twoDecimalPlaces() {
            this.productAmount = this.productAmount.setScale(2, RoundingMode.HALF_UP);
            this.discounts = this.discounts.setScale(2, RoundingMode.HALF_UP);
            this.discountAfter = this.discountAfter.setScale(2, RoundingMode.HALF_UP);
            this.discountCouponAmount = this.discountCouponAmount.setScale(2, RoundingMode.HALF_UP);
            this.discountSeckillAmount = this.discountSeckillAmount.setScale(2, RoundingMode.HALF_UP);
            this.discountProductPromotionAmount = this.discountProductPromotionAmount.setScale(2, RoundingMode.HALF_UP);
            this.discountTimeDiscountAmount = this.discountTimeDiscountAmount.setScale(2, RoundingMode.HALF_UP);
            this.discountDiscountAmount = this.discountDiscountAmount.setScale(2, RoundingMode.HALF_UP);
            this.serviceFee = this.serviceFee.setScale(2, RoundingMode.HALF_UP);
        }

        public void changePrice(BigDecimal lastChangePrice) {
            //优惠后总价
            this.discountAfter = lastChangePrice;
            // 初步计算改价优惠 (商品价格 - 最后改价价格)
            setDiscountChangePrice(getProductAmount()
                    .subtract(lastChangePrice)
                    .setScale(2, RoundingMode.HALF_UP));

            // 得到一个改价优惠后, 进行优惠分摊 (优惠卷,其他优惠等等)
            BigDecimal discountChangePrice = getDiscountChangePrice();
            //总优惠金额
            BigDecimal discounts = Optional.ofNullable(getDiscounts()).orElse(BigDecimal.ZERO);
            //还剩余多少是改价优惠(可能为负数,代表改价改高了)
            BigDecimal subtract = discountChangePrice.subtract(discounts);
            //设置改价优惠
            setDiscountChangePrice(subtract);
            BigDecimal discountDiscountAmount = Optional.ofNullable(getDiscountDiscountAmount()).orElse(BigDecimal.ZERO);
            //排除改价和优惠卷的优惠
            posOtherDiscountAmount = discountDiscountAmount;
            //排除了优惠卷的整个优惠金额
            setDiscountDiscountAmount(discountDiscountAmount.add(subtract));
            // 更新总优惠 = 原有优惠 + 改价优惠
            setDiscounts(discounts.add(subtract));
            twoDecimalPlaces();
        }

    }

    public CartListByStoreVO() {
        this.total = new CartTotal();
        this.cartList = new ArrayList<>();
    }

}