package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 微信分账单 model
 *
 * @author Tigshop团队
 */
@Data
@TableName("profit_sharing_order")
@Schema(description = "微信分账单")
public class ProfitSharingOrder {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "分账单ID")
    private Long id;

    @Schema(description = "商户分账单号(幂等键)")
    private String outOrderNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "微信交易号")
    private String transactionId;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "供应商ID(0表示店铺分账单)")
    private Long vendorId;

    @Schema(description = "结算主体类型:1店铺 2供应商")
    private Integer settlementType;

    @Schema(description = "子商户号")
    private String subMchId;

    @Schema(description = "关联结算单ID")
    private Long settlementId;

    @Schema(description = "手动分账操作人")
    private Long operatorId;

    @Schema(description = "订单总金额(元)")
    private BigDecimal totalAmount;

    @Schema(description = "首次实际分账时的计费基数(元)，历史空值沿用原交易总额")
    private BigDecimal sharingBaseAmount;

    @Schema(description = "分账金额=服务费+分销佣金(元)")
    private BigDecimal splitAmount;

    @Schema(description = "服务费(元)")
    private BigDecimal serviceFee;

    @Schema(description = "分销佣金(元)")
    private BigDecimal commissionFee;

    @Schema(description = "服务费率(%)")
    private BigDecimal serviceFeeRate;

    @Schema(description = "状态:0待分账 1分账中 2成功 3失败")
    private Integer status;

    @Schema(description = "渠道分账单号")
    private String channelOrderId;

    @Schema(description = "完结状态:0未完结 1已完结")
    private Integer finishStatus;

    @Schema(description = "完结时间")
    private Date finishTime;

    @Schema(description = "触发方式:1手动 2自动")
    private Integer triggerType;

    @Schema(description = "结果描述")
    private String resultDesc;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @TableLogic
    @Schema(description = "逻辑删除:0未删 非0已删")
    private Integer isDel;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date modifyTime;
}
