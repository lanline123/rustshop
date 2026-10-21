package com.tigshop.bean.query.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对账单分页查询
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 13:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "对账单分页查询")
public class StatementListPageQuery extends BasePage {
    @Schema(description = "开始时间")
    private String startDateTime;

    @Schema(description = "结束时间")
    private String endDateTime;

    @Schema(description = "账户类型1.账户余额")
    private Integer accountType;

    @Schema(description = "类型：1.手续费 2.店铺提现收支 3.店铺订单收支等")
    private Integer type;

    @Schema(description = "支付方式（如微信、支付宝等）")
    private String paymentType;

    @Schema(description = "单据号")
    private String recordSn;

    @Schema(description = "时间类型 1：入账（结算）时间2：下单时间")
    private Integer timeType;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "供应商id")
    private Integer vendorId;

    @Schema(description = "来源:shop/vendor")
    private String source;
}