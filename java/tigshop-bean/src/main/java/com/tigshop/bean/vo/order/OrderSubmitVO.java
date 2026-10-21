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

import java.math.BigDecimal;

/**
 * @author Tigshop
 */
@Data
@Schema(description = "订单提交返回信息")
public class OrderSubmitVO {

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "返回类型")
    private Integer returnType;

    @Schema(description = "订单ID")
    private String orderSn;

    @Schema(description = "拼团活动信息")
    private Long grouponPromotionRecordId;

    @Schema(description = "余额支付的金额")
    private BigDecimal balance;

    @Schema(description = "是否为次卡兑换的 0 元子订单（true 时前端跳过支付页直接进订单详情）")
    private Boolean isCardRedeem;

}
