package com.tigshop.bean.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账期结算VO
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Data
@Schema(description = "账期结算")
public class CreditSettlementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "结算ID")
    private Integer settlementId;

    @Schema(description = "回款流水单号")
    private String settlementSn;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "会员用户名")
    private String username;

    @Schema(description = "结算金额")
    private BigDecimal settlementAmount;

    @Schema(description = "结算订单数")
    private Integer orderCount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "结算时间")
    private String settlementTime;

    @Schema(description = "结算状态：1-已结算，2-已撤销")
    private Integer settlementStatus;

    @Schema(description = "结算状态文本")
    private String settlementStatusText;

    @Schema(description = "撤销时间")
    private String cancelTime;

    @Schema(description = "撤销原因")
    private String cancelReason;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "创建时间")
    private String createTime;
}
