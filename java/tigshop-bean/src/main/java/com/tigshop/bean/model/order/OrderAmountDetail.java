// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单店铺金额明细
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_amount_detail")
@Schema(description = "订单店铺金额明细")
public class OrderAmountDetail implements Serializable {

    @Schema(description = "订单店铺金额主键")
    @TableId(type = IdType.AUTO)
    private Integer orderDiscountDetailId;

    @Schema(description = "订单主键")
    private Integer orderId;

    @Schema(description = "店铺主键")
    private Integer shopId;

    @Schema(description = "运费金额")
    private BigDecimal shippingFee;

    @Schema(description = "除优惠券之外的金额")
    private BigDecimal discountAmount;

    @Schema(description = "优惠券金额")
    private BigDecimal couponAmount;

    @Schema(description = "限时折扣金额")
    private BigDecimal timeDiscountAmount;

}