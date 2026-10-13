package com.tigshop.bean.vo.shop;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateFormatSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺提现汇总VO
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺提现汇总")
public class ShopWithdrawSummaryVO {

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "店铺logo")
    private String shopLogo;

    @Schema(description = "店铺状态：1开业 4暂停运营 10关店")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusText;

    @Schema(description = "剩余资金（可用余额）")
    private BigDecimal shopMoney;

    @Schema(description = "已提现金额（状态为已完成的提现金额汇总）")
    private BigDecimal withdrawnAmount;

    @Schema(description = "待提现金额（状态为待审核或待打款的提现金额汇总）")
    private BigDecimal pendingWithdrawAmount;

    @JsonSerialize(using = TimestampToDateFormatSerializer.class)
    @Schema(description = "开店时间")
    private Long addTime;
}
