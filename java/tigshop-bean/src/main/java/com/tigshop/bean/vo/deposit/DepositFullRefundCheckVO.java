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
import java.util.List;

/**
 * 店铺/供应商保证金「能否全额退还」预检结果VO。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "保证金能否全额退还预检结果VO")
public class DepositFullRefundCheckVO {

    @Schema(description = "是否可全额退还")
    private Boolean canRefund;

    @Schema(description = "可全额退还金额（可退时为有效可退 max(0,可提现−待审核退还)，不可退时为0）")
    private BigDecimal refundableAmount;

    @Schema(description = "售后限制天数（末单确认收货满 xxx 天，前端文案用）")
    private Integer afterSalesLimitDays;

    @Schema(description = "不满足全额退还的原因（可退时为空）")
    private List<String> unmetReasons;
}
