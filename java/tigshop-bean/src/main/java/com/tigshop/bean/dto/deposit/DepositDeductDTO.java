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
 * 平台端赔付扣除参数。扣减店铺实缴 paid（出账），走账户记账核心 changeDeposit(DEDUCT)。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "赔付扣除参数")
public class DepositDeductDTO {

    @Schema(description = "持有者类型 DepositOwnerType：null=店铺(兼容) 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId；为空时回退使用 shopId）")
    private Integer ownerId;

    @Schema(description = "店铺id（兼容字段；historical 仅传 shopId 时视为店铺维度）")
    private Integer shopId;

    @Schema(description = "扣除金额（正数）")
    private BigDecimal amount;

    @Schema(description = "扣除原因")
    private String reason;

    @Schema(description = "关联订单id（可选，无则传0或不传）")
    private Integer orderId;
}
