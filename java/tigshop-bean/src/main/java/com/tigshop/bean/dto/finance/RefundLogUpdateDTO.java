// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
  * 退款记录更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "退款记录参数")
public class RefundLogUpdateDTO {
    @Schema(description = "接口退款日志ID")
    private Integer logId;

    @Schema(description = "来自的退款申请ID")
    private Integer refundId;

    @Schema(description = "退款类型：1：接口原路退回，2：余额退回，3：线下退回")
    private Integer refundType;

    @Schema(description = "接口退款时的支付方式code，比如wechat、alipay等")
    private String refundPayCode;

    @Schema(description = "接口退款时的支付流水号")
    private String transactionId;

    @Schema(description = "实际退款金额")
    private java.math.BigDecimal refundAmount;

    @Schema(description = "退款时间")
    private Integer addTime;

    @Schema(description = "退款描述")
    private String description;

    @Schema(description = "退款会员ID")
    private Integer userId;

    @Schema(description = "退款的订单ID")
    private Integer orderId;
}
