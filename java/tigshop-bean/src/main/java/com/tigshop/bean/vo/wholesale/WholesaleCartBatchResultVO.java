package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * C 端批量加车结果 VO
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批量加车结果")
public class WholesaleCartBatchResultVO {

    @Schema(description = "成功数")
    private Integer successCount;

    @Schema(description = "失败数")
    private Integer failedCount;

    @Schema(description = "失败明细")
    private List<FailedItem> failedItems;

    /**
     * 批量加车失败明细项
     */
    @Data
    @Schema(description = "批量加车失败明细项")
    public static class FailedItem {

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "SKU ID")
        private Integer skuId;

        @Schema(description = "失败原因")
        private String reason;

        @Schema(description = "业务错误码")
        private Integer code;
    }
}
