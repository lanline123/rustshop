package com.tigshop.bean.query.salesman;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分销员明细列表查询条件
 *
 * @author kidd
 * @since 2025/6/21
 */
@Schema(description = "分销员明细列表查询条件")
@EqualsAndHashCode(callSuper = true)
@Data
public class SalesmanStatisticalDetailPageQuery extends BasePage {

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "分组ID")
    private Integer groupId;

    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "是否导出")
    private Integer isExport;

}
