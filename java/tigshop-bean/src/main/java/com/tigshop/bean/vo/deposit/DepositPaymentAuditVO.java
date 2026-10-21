// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺/供应商端「款项审核」合并行VO（充值单 + 退还单统一展示）。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺/供应商端款项审核合并行VO")
public class DepositPaymentAuditVO {

    @Schema(description = "审核类型 1充值 2退还")
    private Integer auditType;

    @Schema(description = "审核类型文案 保证金充值/保证金退还")
    private String auditTypeText;

    @Schema(description = "业务单id（充值=充值单id，退还=退还单id；查详情用）")
    private Integer bizId;

    @Schema(description = "交易金额（充值=充值金额，退还=退还金额）")
    private BigDecimal amount;

    @Schema(description = "审核状态 0待审核 1已通过 2未通过")
    private Integer status;

    @Schema(description = "审核状态文案 待审核/已通过/未通过")
    private String statusText;

    @Schema(description = "退还类型 1部分退还 2全部退还（仅退还行有值）")
    private Integer refundType;

    @Schema(description = "退还类型文案（仅退还行有值）")
    private String refundTypeText;

    @Schema(description = "备注（充值=申请备注，退还=退还原因）")
    private String remark;

    @Schema(description = "关联凭证（充值=转账凭证，退还=打款凭证）")
    private String payVoucher;

    @Schema(description = "审核备注/驳回原因")
    private String auditRemark;

    @Schema(description = "审核时间(秒级时间戳)")
    private Long auditTime;

    @Schema(description = "审核时间文本")
    private String auditTimeText;

    @Schema(description = "提交时间(秒级时间戳)")
    private Long addTime;

    @Schema(description = "提交时间文本")
    private String addTimeText;
}
