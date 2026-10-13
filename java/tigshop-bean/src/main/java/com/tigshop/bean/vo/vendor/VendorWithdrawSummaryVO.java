package com.tigshop.bean.vo.vendor;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateFormatSerializer;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商提现汇总VO
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "供应商提现汇总")
public class VendorWithdrawSummaryVO {

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "供应商名称")
    private String vendorName;

    @Schema(description = "供应商logo")
    private String vendorLogo;

    @Schema(description = "供应商状态：1开启 2关闭")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusText;

    @Schema(description = "剩余资金（可用余额）")
    private BigDecimal vendorMoney;

    @Schema(description = "已提现金额（状态为已完成的提现金额汇总）")
    private BigDecimal withdrawnAmount;

    @Schema(description = "待提现金额（状态为待审核或待打款的提现金额汇总）")
    private BigDecimal pendingWithdrawAmount;

    @JsonSerialize(using = TimestampToDateFormatSerializer.class)
    @Schema(description = "新增时间")
    private Long addTime;
}
