package com.tigshop.bean.param.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量发起分账参数
 *
 * <p>单体场景下 settlementIds 即为订单ID列表（每订单一条分账单，
 * 服务费取自该订单的 statement SERVICE_FEE 分录）。</p>
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "批量发起分账参数")
public class ProfitSharingSubmitParam {

    @NotEmpty(message = "请选择要发起分账的订单")
    @Schema(description = "订单ID列表（兼容微服务结算单ID语义）")
    private List<Long> settlementIds;
}
