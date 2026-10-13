package com.tigshop.bean.bo.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分销设置
 *
 * @author kidd
 * @since 2025/6/21 09:13
 */
@Data
public class SalesmanConfigBO {

    @Schema(description = "分销模式设置代码")
    private String code;

    @Schema(description = "销售类型")
    private Integer saleType;

    @Schema(description = "分销员级别")
    private List<SalesmanConfigLevelBO> level;

}
