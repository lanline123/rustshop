package com.tigshop.bean.bo.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分销设置等级
 *
 * @author kidd
 * @since 2025/6/21 09:23
 */
@Data
public class SalesmanConfigLevelBO {

    @Schema(description = "分销员级别ID")
    private Integer id;

    @Schema(description = "分销员级别名称")
    private String name;

    @Schema(description = "分销员级别比例")
    private String rate;

    @Schema(description = "下级分销员比例")
    private String downSalesmanRate;

    @Schema(description = "条件设置")
    private SalesmanConfigConditionBO condition;

}
