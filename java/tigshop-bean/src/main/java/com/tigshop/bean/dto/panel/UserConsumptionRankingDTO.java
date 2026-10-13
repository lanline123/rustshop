package com.tigshop.bean.dto.panel;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop
 * @create 2025年03月03日 16:09
 */
@Data
@Schema(description = "排行DTO")
public class UserConsumptionRankingDTO extends BasePage {
    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "是否导出")
    private Integer isExport;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页大小")
    private Integer size;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序顺序")
    private String sortOrder;
}
