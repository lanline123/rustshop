package com.tigshop.bean.query.finance;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退款申请列表
 *
 * @author kidd
 * @create 2025/7/7
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "退款申请列表参数")
public class RefundApplyPageQuery extends BasePage {

    @Schema(description = "退款类型")
    private Integer refundStatus;
}
