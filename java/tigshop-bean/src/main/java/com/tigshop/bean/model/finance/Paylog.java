// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 交易日志model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("paylog")
@Schema(description = "交易日志")
public class Paylog {
    @TableId(value = "paylog_id", type = IdType.AUTO)
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
    private BigDecimal orderAmount;

    @Schema(description = "订单类型：0商品订单，1充值，2付费会员")
    private Integer orderType;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付状态：0:待支付，1:已支付，2:支付失败")
    private Integer payStatus;

    @Schema(description = "支付方式代码，例如wechat,balance")
    private String payCode;

    @Schema(description = "支付时线上分账渠道快照：WECHAT/HUIFU/ALIPAY，空表示未冻结分账资金")
    private String profitSharingChannel;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "交易流水号")
    private String transactionId;

    @Schema(description = "回调参数")
    private String notifyData;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "令牌代码")
    private String tokenCode;
}
