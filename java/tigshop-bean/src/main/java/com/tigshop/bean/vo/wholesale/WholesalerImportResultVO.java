package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量导入批发商结果 VO
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批量导入批发商结果")
public class WholesalerImportResultVO {

    @Schema(description = "请求总数")
    private Integer totalRequested;

    @Schema(description = "成功数")
    private Integer successCount;

    @Schema(description = "跳过数（已是批发商被覆盖等级 或 跳过）")
    private Integer skippedCount;

    @Schema(description = "失败数")
    private Integer failedCount;

    @Schema(description = "失败明细")
    private List<FailedItem> failedDetails;

    /**
     * 导入失败明细项
     */
    @Data
    @Schema(description = "导入失败明细项")
    public static class FailedItem {

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "失败原因")
        private String reason;
    }
}
