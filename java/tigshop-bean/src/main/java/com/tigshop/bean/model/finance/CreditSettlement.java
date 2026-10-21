package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账期结算记录实体
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@TableName("credit_settlement")
@Schema(description = "账期结算记录")
public class CreditSettlement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "settlement_id", type = IdType.AUTO)
    @Schema(description = "结算ID")
    private Integer settlementId;

    @Schema(description = "回款流水单号")
    private String settlementSn;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "授信会员ID")
    private Integer creditUserId;

    @Schema(description = "结算金额（单位：元）")
    private BigDecimal settlementAmount;

    @Schema(description = "结算订单数")
    private Integer orderCount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "结算时间")
    private Long settlementTime;

    @Schema(description = "结算状态：1-已结算，2-已撤销")
    private Integer settlementStatus;

    @Schema(description = "撤销时间")
    private Long cancelTime;

    @Schema(description = "撤销原因")
    private String cancelReason;

    @Schema(description = "操作人ID")
    private Integer operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
