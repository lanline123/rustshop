// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.finance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 交易日志VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "交易日志参数")
public class PaylogVO {
    @Schema(description = "支付日志ID")
    private Integer paylogId;

    @Schema(description = "支付号")
    private String paySn;

    @Schema(description = "支付方式名称")
    private String payName;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单SN")
    private String orderSn;

    @Schema(description = "订单金额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private java.math.BigDecimal orderAmount;

    @Schema(description = "订单类型：0订单，1充值")
    private Integer orderType;

    @Schema(description = "支付金额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private java.math.BigDecimal payAmount;

    @Schema(description = "支付状态：0:待支付，1:已支付，2:支付失败")
    private Integer payStatus;

    @Schema(description = "支付状态")
    private String payStatusName;

    @Schema(description = "支付方式代码，例如wechat")
    private String payCode;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "交易流水号")
    private String transactionId;

    @Schema(description = "回调参数")
    private String notifyData;

    @Schema(description = "退款金额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private java.math.BigDecimal refundAmount;

    @Schema(description = "令牌代码")
    private String tokenCode;

    @Schema(description = "收件人")
    private String consignee;
}