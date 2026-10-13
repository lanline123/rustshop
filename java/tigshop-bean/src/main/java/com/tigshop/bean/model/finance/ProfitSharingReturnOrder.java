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
 * 微信分账回退单 model
 *
 * @author Tigshop团队
 */
@Data
@TableName("profit_sharing_return_order")
@Schema(description = "微信分账回退单")
public class ProfitSharingReturnOrder {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "分账回退单ID")
    private Long id;

    @Schema(description = "关联分账单ID")
    private Long profitSharingOrderId;

    @Schema(description = "商户回退单号")
    private String outReturnNo;

    @Schema(description = "原分账单号")
    private String outOrderNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "退款申请ID")
    private Long refundApplyId;

    @Schema(description = "子商户号")
    private String subMchId;

    @Schema(description = "售后单ID(兼容)")
    private Long aftersaleId;

    @Schema(description = "回退金额(元)")
    private BigDecimal returnAmount;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "状态:0待处理 1处理中 2成功 3失败")
    private Integer status;

    @Schema(description = "渠道回退单号")
    private String channelReturnId;

    @Schema(description = "微信交易号")
    private String transactionId;

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
