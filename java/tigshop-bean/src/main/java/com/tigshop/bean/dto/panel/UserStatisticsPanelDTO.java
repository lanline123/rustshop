package com.tigshop.bean.dto.panel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop
 * @create 2025年03月03日 16:09
 */
@Data
@Schema(description = "用户统计")
public class UserStatisticsPanelDTO {
    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "是否导出")
    private Integer isExport;

    @Schema(description = "店铺ID")
    private Integer shopId;

}
