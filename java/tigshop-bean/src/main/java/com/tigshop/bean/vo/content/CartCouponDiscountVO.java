// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.content;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/4/2 14:29
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "购物车优惠券优惠")
public class CartCouponDiscountVO {
    @Schema(description = "最少订单金额")
    private BigDecimal minOrderAmount;
    @Schema(description = "优惠券金额")
    private BigDecimal couponMoney;
    @Schema(description = "优惠单位：1 元  2 件")
    private Integer couponUnit;
    @Schema(description = "")
    private BigDecimal productPrice;
    @Schema(description = "")
    private Integer quantityCount;
    @Schema(description = "")
    private BigDecimal discountMoney;
}
