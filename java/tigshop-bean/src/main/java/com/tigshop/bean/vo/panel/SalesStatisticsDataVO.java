package com.tigshop.bean.vo.panel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Tigshop团队
 * @create 2025/4/1 16:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "统计")
public class SalesStatisticsDataVO {
    @Schema(description = "新增会员趋势统计图")
    private SaleDataVO salesData;
    @Schema(description = "趋势统计图")
    private AxisVO salesStatisticsData;
}
