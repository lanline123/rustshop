package com.tigshop.bean.param.deposit;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 店铺端保证金流水/冻结明细分页查询参数。
 *
 * <p>shopId 不在本参数中，由控制器从店铺端鉴权头解析（HeaderUtils.getShopId），
 * 避免越权查询他店流水。type/tradeNo/时间筛选对 logList 与 frozenDetailList 均生效。</p>
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺端保证金流水查询参数")
public class DepositShopLogQueryParam extends BasePage {

    @Schema(description = "资金类型/交易类型筛选(DepositLogType,可空;提现/退还暂合并为2)")
    private Integer type;

    @Schema(description = "业务编号(按订单号order_id或关联单号related_id精确匹配,可空)")
    private String tradeNo;

    @Schema(description = "时间起(秒级时间戳,按add_time过滤,可空)")
    private Long startTime;

    @Schema(description = "时间止(秒级时间戳,可空)")
    private Long endTime;
}
