// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.finance.refundapply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退款申请创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "退款申请参数")
public class RefundApplySaveParam {

    @Schema(description = "退款类型：1:订单,2:商品")
    private Integer refundType;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "售后ID")
    private Integer aftersaleId;

    @Schema(description = "退款状态：0:未处理,1:处理中，2:已处理,3:取消")
    private Integer refundStatus;

    @Schema(description = "添加时间")
    private Integer addTime;

    @Schema(description = "回复内容")
    private String refundNote;

    @Schema(description = "线上退款金额")
    private java.math.BigDecimal onlineBalance;

    @Schema(description = "线下退款金额")
    private java.math.BigDecimal offlineBalance;

    @Schema(description = "退款余额")
    private java.math.BigDecimal refundBalance;

    @Schema(description = "线上金额是否到账:0 无需处理1处理中 2已处理")
    private Integer isOnline;

    @Schema(description = "线下金额是否到账:0 无需处理1处理中 2已处理")
    private Integer isOffline;

    @Schema(description = "余额是否到账:0 无需处理1处理中 2已处理")
    private Integer isReceive;

    @Schema(description = "线上退款的关联ID")
    private Integer paylogRefundId;

    @Schema(description = "店铺ID")
    private Integer shopId;
}
