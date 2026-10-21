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
 * 订单店铺优惠券
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_coupon_detail")
@Schema(description = "订单店铺优惠券")
public class OrderCouponDetail implements Serializable {

    @Schema(description = "订单店铺优惠券ID")
    @TableId(type = IdType.AUTO)
    private Integer orderCouponDetailId;

    @Schema(description = "订单主键")
    private Integer orderId;

    @Schema(description = "店铺主键")
    private Integer shopId;

    @Schema(description = "优惠券id")
    private Integer couponId;

    @Schema(description = "优惠金额")
    private BigDecimal couponFee;

}