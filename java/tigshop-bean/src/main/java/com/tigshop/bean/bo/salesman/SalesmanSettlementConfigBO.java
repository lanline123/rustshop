package com.tigshop.bean.bo.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分销结算方案设置
 *
 * @author kidd
 * @since 2025/6/21 09:25
 */
@Data
public class SalesmanSettlementConfigBO {

    @Schema(description = "分销结算方案设置代码")
    private String code;

    @Schema(description = "结算类型；1-自动结算，2-人工结算")
    private Integer settlementType;

    @Schema(description = "自动结算日期类型")
    private Integer dateType;

    @Schema(description = "人工结算说明")
    private String desc;

}
