// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单店铺优惠券VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单店铺优惠券参数")
public class OrderCouponDetailVO {
    @Schema(description = "订单店铺优惠券ID")
    private Integer orderCouponDetailId;

    @Schema(description = "订单店铺优惠券名称")
    private String orderCouponDetailName;

    @Schema(description = "订单店铺优惠券图片")
    private String orderCouponDetailPic;

    @Schema(description = "订单店铺优惠券排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "状态")
    private Integer status;
}