// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 店铺保证金退还/提现申请参数（店铺端）。shopId 取自登录态，不在入参。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺保证金退还/提现申请参数")
public class DepositRefundApplyDTO {

    @Schema(description = "退还类型 1提现 2全额退还(DepositRefundType)")
    private Integer type;

    @Schema(description = "退还金额")
    private BigDecimal amount;

    @Schema(description = "收款信息")
    private String payeeInfo;

    @Schema(description = "退还原因")
    private String reason;
}
